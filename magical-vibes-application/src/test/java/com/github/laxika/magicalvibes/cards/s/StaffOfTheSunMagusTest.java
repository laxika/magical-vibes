package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTheSunMagus.class, Soulmender.class, KalonianTusker.class, Plains.class, Island.class})
class StaffOfTheSunMagusTest extends BaseCardTest {

    private void addStaff() {
        harness.addToBattlefield(player1, new StaffOfTheSunMagus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains 1 life when you cast a white spell")
    void gainsLifeOnWhiteSpell() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new Soulmender(), "{W}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // resolve cast trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when you cast a non-white spell")
    void noLifeOnNonWhiteSpell() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Plains you control enters")
    void gainsLifeWhenPlainsEnters() {
        addStaff();
        harness.setHand(player1, List.of(new Plains()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when a non-Plains land enters")
    void noLifeOnNonPlainsLand() {
        addStaff();
        harness.setHand(player1, List.of(new Island()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casting a white spell does not trigger")
    void opponentWhiteSpellDoesNotTrigger() {
        addStaff();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new Soulmender(), "{W}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Plains entering does not trigger")
    void opponentPlainsDoesNotTrigger() {
        addStaff();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Plains()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Each Staff triggers separately for a white spell")
    void multipleStaffsTriggerForWhiteSpell() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheSunMagus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new Soulmender(), "{W}");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Plains trigger still gains life after the Staff leaves")
    void plainsTriggerSurvivesSourceLeaving() {
        addStaff();
        harness.setHand(player1, List.of(new Plains()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof StaffOfTheSunMagus);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting the colorless Staff does not trigger another Staff")
    void castingStaffDoesNotTrigger() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new StaffOfTheSunMagus(), "{3}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
