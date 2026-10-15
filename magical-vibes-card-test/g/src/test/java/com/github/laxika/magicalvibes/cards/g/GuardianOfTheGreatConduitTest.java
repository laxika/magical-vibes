package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NissaGenesisMage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfTheGreatConduit.class, NissaGenesisMage.class})
class GuardianOfTheGreatConduitTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and vigilance while its controller controls a Nissa planeswalker")
    void getsBonusWithNissa() {
        harness.addToBattlefield(player1, new GuardianOfTheGreatConduit());
        harness.addToBattlefield(player1, new NissaGenesisMage());

        Permanent guardian = findPermanent(player1, "Guardian of the Great Conduit");

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus without a Nissa planeswalker")
    void hasNoBonusWithoutNissa() {
        harness.addToBattlefield(player1, new GuardianOfTheGreatConduit());

        Permanent guardian = findPermanent(player1, "Guardian of the Great Conduit");

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not count a Nissa planeswalker controlled by an opponent")
    void opponentNissaDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new GuardianOfTheGreatConduit());
        harness.addToBattlefield(player2, new NissaGenesisMage());

        Permanent guardian = findPermanent(player1, "Guardian of the Great Conduit");

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Nissa planeswalker leaves the battlefield")
    void losesBonusWhenNissaLeaves() {
        harness.addToBattlefield(player1, new GuardianOfTheGreatConduit());
        harness.addToBattlefield(player1, new NissaGenesisMage());

        Permanent guardian = findPermanent(player1, "Guardian of the Great Conduit");
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof NissaGenesisMage);

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isFalse();
    }
}
