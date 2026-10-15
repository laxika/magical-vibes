package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Fatestitcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuppressionField;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighwayReaver.class, GrizzlyBears.class, Fatestitcher.class})
class HighwayReaverTest extends BaseCardTest {

    @Test
    void etbPerpetuallyGrantsUnearthOnlyToCreatureWithoutUnearth() {
        GrizzlyBears bears = new GrizzlyBears();
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(bears, fatestitcher));
        harness.castFromHand(player1, new HighwayReaver(), "{2}{B}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedPerpetualUnearth).contains(bears.getId());
        assertThat(gd.cardsGrantedPerpetualUnearth).doesNotContain(fatestitcher.getId());
    }

    @Test
    void maxSpeedMakesFirstGrantedUnearthFree() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new HighwayReaver(), "{2}{B}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        gd.playerSpeeds.put(player1.getId(), 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playersWhoUsedMaxSpeedFreeUnearthThisTurn).contains(player1.getId());
    }

    @Test
    void attackGrantsUnearthWithTheCardsColoredManaCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefield(player1, new HighwayReaver());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void unearthBeforeReaverEntersStillCountsAsTheFirstActivation() {
        Fatestitcher first = new Fatestitcher();
        Fatestitcher second = new Fatestitcher();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.castFromHand(player1, new HighwayReaver(), "{2}{B}{R}");
        harness.passBothPriorities();
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    @CardUsed({SuppressionField.class})
    void zeroUnearthCostStillRequiresPayingActivationTaxes() {
        harness.addToBattlefield(player1, new HighwayReaver());
        harness.addToBattlefield(player2, new SuppressionField());
        harness.setGraveyard(player1, List.of(new Fatestitcher()));
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fatestitcher");
    }
}
