package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreymondAvacynsStalwart.class, EliteVanguard.class, GrizzlyBears.class})
class GreymondAvacynsStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("First strike and vigilance choice are granted to Humans you control")
    void firstStrikeAndVigilanceChoice() {
        Permanent greymond = castGreymond("First strike and vigilance");
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, greymond, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greymond, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greymond, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike and lifelink choice grants the selected abilities")
    void firstStrikeAndLifelinkChoice() {
        Permanent greymond = castGreymond("First strike and lifelink");
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, greymond, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greymond, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, greymond, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Four Humans get +2/+2, and the bonus is removed below four")
    void fourHumansGetPlusTwoPlusTwo() {
        Permanent greymond = castGreymond("Vigilance and lifelink");
        Permanent firstHuman = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent secondHuman = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent thirdHuman = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, greymond)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, greymond)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, firstHuman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstHuman)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(thirdHuman);

        assertThat(gqs.getEffectivePower(gd, greymond)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greymond)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondHuman)).isEqualTo(1);
    }

    private Permanent castGreymond(String choice) {
        harness.setHand(player1, java.util.List.of(new GreymondAvacynsStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, choice);
        return findPermanent(player1, "Greymond, Avacyn's Stalwart");
    }
}
