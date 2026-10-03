package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodswornSteward.class, GrizzlyBears.class})
class BloodswornStewardTest extends BaseCardTest {

    @Test
    void boostsYourCommanderCreaturesAndGivesThemHasteOnly() {
        harness.addToBattlefield(player1, new BloodswornSteward());
        Card ownCommanderCard = new GrizzlyBears();
        Permanent ownCommander = harness.addToBattlefieldAndReturn(player1, ownCommanderCard);
        Permanent ownNonCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card opposingCommanderCard = new GrizzlyBears();
        Permanent opposingCommander = harness.addToBattlefieldAndReturn(player2, opposingCommanderCard);
        gd.makeCommander(player1.getId(), ownCommanderCard);
        gd.makeCommander(player2.getId(), opposingCommanderCard);

        assertThat(gqs.getEffectivePower(gd, ownCommander)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCommander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCommander, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownNonCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownNonCommander, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCommander, Keyword.HASTE)).isFalse();
    }

    @Test
    void boostsItselfWhenItIsYourCommander() {
        BloodswornSteward card = new BloodswornSteward();
        Permanent steward = harness.addToBattlefieldAndReturn(player1, card);
        gd.makeCommander(player1.getId(), card);

        assertThat(gqs.getEffectivePower(gd, steward)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, steward)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, steward, Keyword.HASTE)).isTrue();
    }
}
