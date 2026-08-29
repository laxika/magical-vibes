package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SunstreakPhoenix.class)
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

    private void makeItNight() {
        harness.performUntapStep(player1);
        harness.passBothPriorities();
    }
}
