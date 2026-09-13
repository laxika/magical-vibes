package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinedFluke.class, CoralMerfolk.class})
class SpinedFlukeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifices Spined Fluke itself when it is the only creature")
    void etbSacrificesItselfWhenOnlyCreature() {
        harness.setHand(player1, List.of(new SpinedFluke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spined Fluke");
        harness.assertInGraveyard(player1, "Spined Fluke");
    }

    @Test
    @DisplayName("ETB lets the controller choose another creature to sacrifice")
    void etbControllerChoosesAnotherCreature() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new SpinedFluke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        Permanent merfolk = findPermanent(player1, "Coral Merfolk");
        harness.handlePermanentChosen(player1, merfolk.getId());

        harness.assertOnBattlefield(player1, "Spined Fluke");
        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("ETB sacrifices only a creature controlled by Spined Fluke's controller")
    void etbDoesNotSacrificeAnOpponentsCreature() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new SpinedFluke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spined Fluke");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Activating {B} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent perm = addCreatureReady(player1, new SpinedFluke());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Spined Fluke from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new SpinedFluke());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        CoralMerfolk attackerCard = new CoralMerfolk();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        Permanent survivor = findPermanent(player1, "Spined Fluke");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }
}
