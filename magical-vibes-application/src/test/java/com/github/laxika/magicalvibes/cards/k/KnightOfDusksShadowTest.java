package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.PrayerOfBinding;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfDusksShadow.class, PrayerOfBinding.class})
class KnightOfDusksShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponents from gaining life but not its controller")
    void preventsOpponentsFromGainingLife() {
        addKnight();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The activated ability gives the Knight +1/+1 until end of turn")
    void boostsSelf() {
        Permanent knight = addKnight();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent knight = addKnight();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addKnight() {
        return harness.addToBattlefieldAndReturn(player1, new KnightOfDusksShadow());
    }

    @Test
    void preventsActualOpponentLifeGain() {
        addKnight();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player2, new PrayerOfBinding(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void allowsActualControllerLifeGain() {
        addKnight();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new PrayerOfBinding(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    void opponentCanGainLifeAfterKnightIsExiledDuringResolution() {
        Permanent knight = addKnight();
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player2, List.of(new PrayerOfBinding()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player2);

        harness.castEnchantment(player2, 0, knight.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight of Dusk's Shadow");
        harness.assertLife(player2, lifeBefore + 2);
    }

    @Test
    void repeatedActivationsAccumulateWithoutTapping() {
        Permanent knight = addKnight();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(4);
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new KnightOfDusksShadow());
        addCreatureReady(player2, new KnightOfDusksShadow());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new KnightOfDusksShadow());
        Permanent first = addCreatureReady(player2, new KnightOfDusksShadow());
        Permanent second = addCreatureReady(player2, new KnightOfDusksShadow());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
