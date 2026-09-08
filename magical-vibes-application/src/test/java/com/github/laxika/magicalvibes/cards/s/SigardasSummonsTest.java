package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardasSummons.class, GrizzlyBears.class})
class SigardasSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Countered creatures you control become 4/4 Angels with flying")
    void counteredOwnCreaturesBecomeAngelsWithFlying() {
        harness.addToBattlefield(player1, new SigardasSummons());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(creature.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Only your creatures with +1/+1 counters are affected")
    void onlyCounteredOwnCreaturesAreAffected() {
        harness.addToBattlefield(player1, new SigardasSummons());
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, uncountered)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, uncountered)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, uncountered).grantedSubtypes()).doesNotContain(CardSubtype.ANGEL);

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes()).doesNotContain(CardSubtype.ANGEL);
    }
}
