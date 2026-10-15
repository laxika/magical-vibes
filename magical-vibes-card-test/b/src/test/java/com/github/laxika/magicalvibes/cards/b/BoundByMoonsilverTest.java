package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoundByMoonsilver.class, DawntreaderElk.class, HinterlandHermit.class, Forest.class})
class BoundByMoonsilverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches Bound by Moonsilver to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BoundByMoonsilver()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bound by Moonsilver")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted Werewolf cannot transform when no spells were cast last turn")
    void enchantedCreatureCannotTransform() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        attachAura(player2, hermit);

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
    }

    @Test
    @DisplayName("Sacrificing another permanent moves the Aura onto the target creature")
    void sacrificeReattachesAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest()); // only other permanent → auto-sacrificed

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraIndex = indexOf(player1, aura);
        harness.activateAbility(player1, auraIndex, null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void onlyOnceEachTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraIndex = indexOf(player1, aura);
        harness.activateAbility(player1, auraIndex, null, second.getId());
        // Two forests → choose one to sacrifice
        Permanent forest = findPermanent(player1, "Forest");
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, forest.getId());
        }
        harness.passBothPriorities();

        Permanent third = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, third.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated at instant speed")
    void sorcerySpeedOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        Permanent blocker = addCreatureReady(player2, new DawntreaderElk());
        attachAura(player1, blocker);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("The Aura cannot sacrifice itself when it is the only permanent its controller controls")
    void cannotSacrificeItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
        harness.assertOnBattlefield(player1, "Bound by Moonsilver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Moving the Aura releases the original creature and prevents the new creature from attacking")
    void movingAuraTransfersAttackRestriction() {
        Permanent first = addCreatureReady(player2, new DawntreaderElk());
        Permanent second = addCreatureReady(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated during another player's main phase")
    void cannotActivateDuringOpponentsTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
    }

    @Test
    @DisplayName("Ability cannot be activated while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BoundByMoonsilver()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        gs.playCard(gd, player1, 0, 0, second.getId(), null);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();
    }

    private Permanent attachAura(Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new BoundByMoonsilver());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
