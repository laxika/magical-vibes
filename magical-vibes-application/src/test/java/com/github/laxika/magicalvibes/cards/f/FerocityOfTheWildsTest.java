package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerocityOfTheWilds.class, GrizzlyBears.class, YouthfulKnight.class})
class FerocityOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking non-Human creatures you control get +1/+0 and trample")
    void buffsAttackingNonHumans() {
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        Permanent bears = addAttackingCreature(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Humans and nonattacking creatures do not get the effect")
    void excludesHumansAndNonattackers() {
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        Permanent human = addAttackingCreature(player1, new YouthfulKnight());
        Permanent nonattackingBear = new Permanent(new GrizzlyBears());
        nonattackingBear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(nonattackingBear);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, nonattackingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonattackingBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponents' attacking creatures are unaffected")
    void excludesOpponentsAttackers() {
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        Permanent opponentBear = addAttackingCreature(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addAttackingCreature(Player controller, com.github.laxika.magicalvibes.model.Card creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }
}
