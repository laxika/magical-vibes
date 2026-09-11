package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MoonlitAmbusher;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakshadeStalker.class, MoonlitAmbusher.class})
class OakshadeStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Daybound creature transforms into its back face when it becomes night")
    void transformsToBackFaceWhenItBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new OakshadeStalker());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(stalker.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nightbound creature transforms into its front face when it becomes day")
    void transformsToFrontFaceWhenItBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OakshadeStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stalker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(stalker.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(stalker.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can be cast on an opponent's turn by paying two more mana")
    void flashCastForTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OakshadeStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Oakshade Stalker"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be cast on an opponent's turn without the surcharge")
    void noFlashWithoutTheSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OakshadeStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
