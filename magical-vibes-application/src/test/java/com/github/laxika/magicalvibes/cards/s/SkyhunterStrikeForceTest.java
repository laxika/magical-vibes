package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyhunterStrikeForce.class, GrizzlyBears.class})
class SkyhunterStrikeForceTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant gives other creatures you control melee while you control your commander")
    void lieutenantGrantsMeleeWhileControllingCommander() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        harness.addToBattlefield(player1, new SkyhunterStrikeForce());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.MELEE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MELEE)).isFalse();

        harness.addToBattlefield(player1, commanderCard);

        assertThat(gqs.hasKeyword(gd, ally, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant melee grants +1/+1 when attacking an opponent")
    void meleeBoostsAttackingAlly() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        harness.addToBattlefield(player1, new SkyhunterStrikeForce());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, commanderCard);

        int powerBefore = gqs.getEffectivePower(gd, ally);
        int toughnessBefore = gqs.getEffectiveToughness(gd, ally);

        declareAttackers(player1, java.util.List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(toughnessBefore + 1);
    }
}
