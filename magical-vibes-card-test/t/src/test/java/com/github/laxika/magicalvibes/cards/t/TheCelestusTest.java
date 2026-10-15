package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCelestus.class, Forest.class})
class TheCelestusTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new TheCelestus(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent celestus = addReadyCelestus();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(celestus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void changesDayToNight() {
        gd.dayNight = DayNight.DAY;
        Permanent celestus = addReadyCelestus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(celestus.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void changesNightToDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent celestus = addReadyCelestus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(celestus.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void dayNightChangeGainsLifeAndMayDrawThenDiscard() {
        gd.dayNight = DayNight.DAY;
        addReadyCelestus();
        Card drawn = new Forest();
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void enteringAtNightDoesNotChangeDesignationOrTriggerLifeGain() {
        gd.dayNight = DayNight.NIGHT;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new TheCelestus(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningDrawStillGainsLifeWithoutDrawingOrDiscarding() {
        gd.dayNight = DayNight.DAY;
        addReadyCelestus();
        Card inHand = new Forest();
        Card inLibrary = new Forest();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(inLibrary));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(inLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotToggleOutsideMainPhase() {
        gd.dayNight = DayNight.DAY;
        Permanent celestus = addReadyCelestus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(celestus.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void opponentControlledCelestusGainsLifeForItsController() {
        gd.dayNight = DayNight.DAY;
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new TheCelestus());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }

    @Test
    void enteringDuringDayDoesNotTriggerLifeGain() {
        gd.dayNight = DayNight.DAY;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new TheCelestus(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void drawingWithAnEmptyHandRequiresDiscardingTheDrawnCard() {
        gd.dayNight = DayNight.DAY;
        addReadyCelestus();
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void toggleAndLifeGainTriggerResolveSeparately() {
        gd.dayNight = DayNight.DAY;
        addReadyCelestus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCelestus() {
        return harness.addToBattlefieldAndReturn(player1, new TheCelestus());
    }

    private void activateToggle() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
