package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OtherworldlyGaze;
import com.github.laxika.magicalvibes.cards.t.TurnTheEarth;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunstreakPhoenix.class, TurnTheEarth.class, OtherworldlyGaze.class})
class SunstreakPhoenixTest extends BaseCardTest {

    @Test
    void doesNotTriggerWhenDayNightDesignationStarts() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));

        harness.enterBattlefieldAndReturn(player1, new SunstreakPhoenix());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
    }

    @Test
    void paysToReturnFromGraveyardTappedWhenDayBecomesNight() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        gd.dayNight = DayNight.DAY;
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        makeItNight();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId())
                        && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(phoenix);
    }

    @Test
    void decliningKeepsPhoenixInGraveyard() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        gd.dayNight = DayNight.DAY;
        harness.setGraveyard(player1, List.of(phoenix));

        makeItNight();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    void enteringDuringNightDoesNotChangeTheDesignation() {
        gd.dayNight = DayNight.NIGHT;

        harness.enterBattlefieldAndReturn(player1, new SunstreakPhoenix());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void paysToReturnFromGraveyardTappedWhenNightBecomesDay() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        gd.dayNight = DayNight.NIGHT;
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.performUntapStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId())
                        && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(phoenix);
    }

    @Test
    void cannotReturnWithoutEnoughManaForTheGenericCost() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        gd.dayNight = DayNight.DAY;
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 1);

        makeItNight();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void phoenixOnBattlefieldDoesNotTriggerOnDayNightChange() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new SunstreakPhoenix());
        harness.addMana(player1, ManaColor.RED, 2);

        makeItNight();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertOnBattlefield(player1, "Sunstreak Phoenix");
    }

    @Test
    void oldTriggerCannotReturnPhoenixThatLeftAndReenteredGraveyard() {
        SunstreakPhoenix phoenix = new SunstreakPhoenix();
        gd.dayNight = DayNight.DAY;
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TurnTheEarth(), new OtherworldlyGaze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.performUntapStep(player1);
        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(phoenix.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(phoenix);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(phoenix);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
        harness.assertNotOnBattlefield(player1, "Sunstreak Phoenix");
    }

    private void makeItNight() {
        harness.performUntapStep(player1);
        harness.passBothPriorities();
    }
}
