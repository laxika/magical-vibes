package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuxaPatientProfessor.class, GrizzlyBears.class, LlanowarElves.class})
class RuxaPatientProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a creature card with no abilities from its controller's graveyard")
    void returnsVanillaCreatureFromGraveyardOnEnter() {
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        castRuxa();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger returns a creature card with no abilities")
    void returnsVanillaCreatureFromGraveyardOnAttack() {
        addCreatureReady(player1, new RuxaPatientProfessor());
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void boostsOnlyCreaturesWithNoAbilitiesYouControl() {
        addCreatureReady(player1, new RuxaPatientProfessor());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownElf = addCreatureReady(player1, new LlanowarElves());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("A vanilla creature can assign combat damage as though it were unblocked")
    void vanillaCreatureCanAssignCombatDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RuxaPatientProfessor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker),
                Map.of(player2.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castRuxa() {
        harness.setHand(player1, List.of(new RuxaPatientProfessor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
