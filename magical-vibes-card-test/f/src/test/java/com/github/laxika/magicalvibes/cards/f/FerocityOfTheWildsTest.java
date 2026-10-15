package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DanceOfTheManse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerocityOfTheWilds.class, GrizzlyBears.class, YouthfulKnight.class, TuinvaleTreefolk.class})
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
        Permanent nonattackingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        nonattackingBear.setSummoningSick(false);

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

    @Test
    @DisplayName("Bonuses end as soon as the creature stops attacking")
    void losesBonusesWhenNoLongerAttacking() {
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        Permanent treefolk = addAttackingCreature(player1, new TuinvaleTreefolk());

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.TRAMPLE)).isTrue();

        treefolk.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their power bonuses")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        harness.addToBattlefield(player1, new FerocityOfTheWilds());
        Permanent treefolk = addAttackingCreature(player1, new TuinvaleTreefolk());

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @CardUsed({FerocityOfTheWilds.class, DanceOfTheManse.class})
    @DisplayName("An animated Ferocity of the Wilds benefits from its own ability while attacking")
    void animatedEnchantmentBuffsItself() {
        FerocityOfTheWilds ferocity = new FerocityOfTheWilds();
        harness.setGraveyard(player1, List.of(ferocity));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of(ferocity.getId()));
        harness.passBothPriorities();

        Permanent animated = findPermanent(player1, "Ferocity of the Wilds");
        assertThat(gqs.getEffectivePower(gd, animated)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, animated)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, animated, Keyword.TRAMPLE)).isFalse();

        animated.setSummoningSick(false);
        animated.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, animated)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, animated)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, animated, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addAttackingCreature(Player controller, com.github.laxika.magicalvibes.model.Card creature) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, creature);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }
}
