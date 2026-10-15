package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErebossIntervention.class, GrizzlyBears.class, LlanowarElves.class})
class ErebossInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode gives -X/-X and controller gains X life")
    void creatureModeShrinksAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantForX(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Graveyard mode can exile up to twice X cards across graveyards")
    void graveyardModeUsesTwiceXAcrossGraveyards() {
        Card own1 = new GrizzlyBears();
        Card own2 = new GrizzlyBears();
        Card own3 = new GrizzlyBears();
        Card own4 = new GrizzlyBears();
        Card opponent1 = new LlanowarElves();
        Card opponent2 = new LlanowarElves();
        Card opponent3 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(own1, own2, own3, own4));
        harness.setGraveyard(player2, List.of(opponent1, opponent2, opponent3));
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalInstantForX(player1, 0, 1, 3, null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(6);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                own1.getId(), own2.getId(), own3.getId(), own4.getId(),
                opponent1.getId(), opponent2.getId(), opponent3.getId());

        List<UUID> targets = List.of(
                own1.getId(), own2.getId(), own3.getId(),
                opponent1.getId(), opponent2.getId(), opponent3.getId());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .containsExactlyInAnyOrderElementsOf(targets);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(own4.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void creatureModeExpiresAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantForX(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void lethalCreatureModeStillGainsFullXLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castModalInstantForX(player1, 0, 0, 4, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 14);
    }

    @Test
    void creatureModeDoesNotGainLifeWhenItsOnlyTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castModalInstantForX(player1, 0, 0, 2, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void creatureModeWithZeroXDoesNotChangeCreatureOrLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantForX(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertLife(player1, 10);
    }

    @Test
    void graveyardModeCanChooseZeroTargetsWithPositiveX() {
        Card card = new ErebossIntervention();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantForX(player1, 0, 1, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void graveyardModeWithZeroXDoesNotRequireTargets() {
        Card card = new ErebossIntervention();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantForX(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardModeExilesRemainingLegalTargetWhenAnotherLeaves() {
        Card first = new ErebossIntervention();
        Card second = new ErebossIntervention();
        Card unchosen = new ErebossIntervention();
        harness.setGraveyard(player2, List.of(first, second, unchosen));
        harness.setHand(player1, List.of(new ErebossIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castModalInstantForX(player1, 0, 1, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player2, List.of(second, unchosen));
        harness.setHand(player2, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(second.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unchosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
    }
}
