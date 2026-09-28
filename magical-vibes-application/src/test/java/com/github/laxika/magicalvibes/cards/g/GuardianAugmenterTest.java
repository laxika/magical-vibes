package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianAugmenter.class, GrizzlyBears.class})
class GuardianAugmenterTest extends BaseCardTest {

    @Test
    void boostsAndProtectsYourCommanderCreaturesOnly() {
        harness.addToBattlefield(player1, new GuardianAugmenter());
        Card ownCommanderCard = new GrizzlyBears();
        Permanent ownCommander = harness.addToBattlefieldAndReturn(player1, ownCommanderCard);
        Permanent ownNonCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card opposingCommanderCard = new GrizzlyBears();
        Permanent opposingCommander = harness.addToBattlefieldAndReturn(player2, opposingCommanderCard);
        gd.makeCommander(player2.getId(), opposingCommanderCard);

        assertThat(gqs.getEffectivePower(gd, ownCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCommander, Keyword.HEXPROOF)).isFalse();

        gd.makeCommander(player1.getId(), ownCommanderCard);
        assertThat(gqs.getEffectivePower(gd, ownCommander)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCommander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCommander, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownNonCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownNonCommander, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCommander, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void boostsAndProtectsItselfWhenItIsYourCommander() {
        GuardianAugmenter card = new GuardianAugmenter();
        Permanent augmenter = harness.addToBattlefieldAndReturn(player1, card);
        gd.makeCommander(player1.getId(), card);

        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, augmenter, Keyword.HEXPROOF)).isTrue();
    }
}
