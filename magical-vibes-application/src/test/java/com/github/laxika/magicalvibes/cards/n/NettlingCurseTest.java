package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NettlingCurse.class, AssaultZeppelid.class, AzoriusSignet.class})
class NettlingCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature attacking makes its controller lose 3 life")
    void attackingLosesThreeLife() {
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        attachCurse(player1, creature);

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Enchanted creature blocking makes its controller lose 3 life")
    void blockingLosesThreeLife() {
        addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new AssaultZeppelid());
        attachCurse(player2, blocker);

        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The activated ability makes the enchanted creature attack this turn if able")
    void activatedAbilityMakesEnchantedCreatureAttack() {
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        Permanent aura = attachCurse(player1, creature);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Nettling Curse can enchant only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new NettlingCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enchanted creature's controller loses life even when an opponent controls the Aura")
    void attackingCreatureControllerLosesLifeWhenAuraIsOpponentControlled() {
        Permanent creature = addCreatureReady(player2, new AssaultZeppelid());
        attachCurse(player1, creature);

        int lifeBefore = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    private Permanent attachCurse(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new NettlingCurse());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
