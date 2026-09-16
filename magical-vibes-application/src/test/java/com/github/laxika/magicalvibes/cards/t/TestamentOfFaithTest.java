package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TestamentOfFaith.class})
class TestamentOfFaithTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeActivation() {
        Permanent testament = addTestament(player1);

        assertThat(gqs.isCreature(gd, testament)).isFalse();
    }

    @Test
    void activationUsesPaidXAndGrantsWallAndDefender() {
        Permanent testament = addTestament(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, testament)).isTrue();
        assertThat(gqs.isEnchantment(gd, testament)).isTrue();
        assertThat(gqs.getEffectivePower(gd, testament)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, testament)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, testament, CardSubtype.WALL)).isTrue();
        assertThat(gqs.hasKeyword(gd, testament, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void zeroXAnimationPutsTestamentIntoItsGraveyard() {
        addTestament(player1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .singleElement()
                .isInstanceOf(TestamentOfFaith.class);
    }

    @Test
    void animationWearsOffAtEndOfTurn() {
        Permanent testament = addTestament(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, testament)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, testament)).isFalse();
        assertThat(gqs.isEnchantment(gd, testament)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, testament, CardSubtype.WALL)).isFalse();
        assertThat(gqs.hasKeyword(gd, testament, Keyword.DEFENDER)).isFalse();
    }

    private Permanent addTestament(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TestamentOfFaith());
    }
}
