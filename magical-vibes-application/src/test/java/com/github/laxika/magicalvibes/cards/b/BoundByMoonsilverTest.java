package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundByMoonsilverTest extends BaseCardTest {

    // ===== Casting / attach =====

    @Test
    @DisplayName("Resolving attaches Bound by Moonsilver to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setHand(player1, List.of(new BoundByMoonsilver()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bound by Moonsilver")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    // ===== Can't attack / block =====

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        attachAura(player2, bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Can't transform =====

    @Test
    @DisplayName("Enchanted Werewolf cannot transform on its nightbound upkeep")
    void enchantedCreatureCannotTransform() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        attachAura(player2, hermit);

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
    }

    // ===== Activated ability: sacrifice another permanent to reattach =====

    @Test
    @DisplayName("Sacrificing another permanent moves the Aura onto the target creature")
    void sacrificeReattachesAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest()); // only other permanent → auto-sacrificed

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraIndex = indexOf(player1, aura);
        harness.activateAbility(player1, auraIndex, null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void onlyOnceEachTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraIndex = indexOf(player1, aura);
        harness.activateAbility(player1, auraIndex, null, second.getId());
        // Two forests → choose one to sacrifice
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, forest.getId());
        }
        harness.passBothPriorities();

        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, third.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated at instant speed")
    void sorcerySpeedOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, first);
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent attachAura(Player controller, Permanent host) {
        Permanent aura = new Permanent(new BoundByMoonsilver());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
