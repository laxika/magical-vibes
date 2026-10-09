package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.w.WilyBandar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CultivatorsCaravan.class, BastionMastodon.class, WilyBandar.class})
class CultivatorsCaravanTest extends BaseCardTest {

    @Test
    void addsManaOfAnyColor() {
        Permanent caravan = addCreatureReady(player1, new CultivatorsCaravan());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(caravan.isTapped()).isTrue();
    }

    @Test
    void crewAnimatesCaravanAndTapsCrew() {
        Permanent caravan = addCreatureReady(player1, new CultivatorsCaravan());
        Permanent crew = addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(caravan.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(caravan.isTapped()).isFalse();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new CultivatorsCaravan());
        addCreatureReady(player1, new WilyBandar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void newNoncreatureCaravanCanProduceManaImmediately() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(caravan.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesExactlyOneManaOfChosenColor(ManaColor color) {
        Permanent caravan = addCreatureReady(player1, new CultivatorsCaravan());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(caravan.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayCrewCost() {
        addCreatureReady(player1, new CultivatorsCaravan());
        Permanent tappedCrew = addCreatureReady(player1, new BastionMastodon());
        tappedCrew.tap();
        addCreatureReady(player2, new BastionMastodon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void summoningSickCreaturesCanCombineToCrewTappedCaravan() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());
        caravan.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new WilyBandar());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, caravan)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(caravan.isTapped()).isTrue();
    }

    @Test
    void newlyEnteredCaravanCannotUseTapAbilityAfterBeingCrewed() {
        harness.addToBattlefield(player1, new CultivatorsCaravan());
        harness.addToBattlefield(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void animationExpiresAtEndOfTurn() {
        Permanent caravan = addCreatureReady(player1, new CultivatorsCaravan());
        addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, caravan)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, caravan)).isFalse();
    }

    @Test
    void canChooseAdditionalCrewAfterMeetingRequiredPower() {
        addCreatureReady(player1, new CultivatorsCaravan());
        Permanent first = addCreatureReady(player1, new BastionMastodon());
        Permanent second = addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }
}
