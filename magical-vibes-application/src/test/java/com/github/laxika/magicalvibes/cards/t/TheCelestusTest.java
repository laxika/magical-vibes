package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        activateToggle(celestus);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(celestus.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void changesNightToDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent celestus = addReadyCelestus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle(celestus);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(celestus.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void dayNightChangeGainsLifeAndMayDrawThenDiscard() {
        gd.dayNight = DayNight.DAY;
        Permanent celestus = addReadyCelestus();
        Card drawn = new Forest();
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateToggle(celestus);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    private Permanent addReadyCelestus() {
        Permanent celestus = new Permanent(new TheCelestus());
        celestus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(celestus);
        return celestus;
    }

    private void activateToggle(Permanent celestus) {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
