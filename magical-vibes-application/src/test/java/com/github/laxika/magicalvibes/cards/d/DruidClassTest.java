package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidClass.class, Forest.class})
class DruidClassTest extends BaseCardTest {

    @Test
    void landfallGainsOneLife() {
        harness.addToBattlefield(player1, new DruidClass());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    void levelTwoAllowsAnAdditionalLandPlay() {
        Permanent druidClass = harness.addToBattlefieldAndReturn(player1, new DruidClass());
        levelUpToTwo(druidClass);

        assertThat(druidClass.getCounterCount(CounterType.LEVEL)).isEqualTo(1);

        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Forest".equals(permanent.getCard().getName())))
                .hasSize(2);
    }

    @Test
    void levelThreeAnimatesOnlyAControlledLandWithDynamicPowerAndToughness() {
        Permanent druidClass = harness.addToBattlefieldAndReturn(player1, new DruidClass());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        levelUpToThree(druidClass);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
        assertThat(choice.validIds()).doesNotContain(opponentLand.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }

    private void levelUpToTwo(Permanent druidClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, battlefieldIndex(druidClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent druidClass) {
        levelUpToTwo(druidClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, battlefieldIndex(druidClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
