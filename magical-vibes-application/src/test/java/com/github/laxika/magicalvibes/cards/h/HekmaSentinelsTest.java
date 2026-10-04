package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.u.Unburden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HekmaSentinels.class, Censor.class, DoomedDissenter.class, Unburden.class})
class HekmaSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature +1/+1")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(1);
        assertThat(hekma.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each discard stacks another +1/+1")
    void discardsStack() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new DoomedDissenter(), new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(2);
        assertThat(hekma.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(hekma.getPowerModifier()).isEqualTo(0);
        assertThat(hekma.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Discarding two cards without cycling creates two separate boosts")
    void ordinaryDiscardsBoostSelfForEachCard() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new DoomedDissenter(), new Censor()));
        harness.setHand(player2, List.of(new Unburden()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(hekma.getPowerModifier()).isEqualTo(1);
        assertThat(hekma.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(hekma.getPowerModifier()).isEqualTo(2);
        assertThat(hekma.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent cycling does not boost this creature")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new DoomedDissenter()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(getHekmaSentinels().getPowerModifier()).isZero();
        assertThat(getHekmaSentinels().getToughnessModifier()).isZero();
        harness.assertInHand(player2, "Doomed Dissenter");
    }

    private Permanent getHekmaSentinels() {
        return findPermanent(player1, "Hekma Sentinels");
    }
}
