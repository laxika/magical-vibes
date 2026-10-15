package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorceressQueen;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnHavvaConstable.class, GrizzlyBears.class, SorceressQueen.class, Terror.class})
class AnHavvaConstableTest extends BaseCardTest {

    @Test
    @DisplayName("Green creature cards outside the battlefield do not count")
    void greenCardsOutsideBattlefieldDoNotCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setExile(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("Toughness increases when a green creature resolves, not while it is on the stack")
    void toughnessUpdatesWhenGreenCreatureEnters() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);
    }

    @Test
    @DisplayName("Toughness decreases immediately when an opponent's green creature dies")
    void toughnessUpdatesWhenGreenCreatureDies() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("Alone it counts itself: 2/2")
    void aloneCountsItself() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each green creature adds one toughness")
    void greenCreaturesAddToughness() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(4);
    }

    @Test
    @DisplayName("Green creatures on any battlefield count")
    void opponentGreenCreaturesCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green creatures do not count")
    void nonGreenCreaturesDontCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new SorceressQueen());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("A base P/T setter overrides its characteristic-defining toughness")
    void basePowerToughnessSetterOverridesCharacteristicDefiningToughness() {
        addCreatureReady(player1, new SorceressQueen());
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, constable.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }
}
