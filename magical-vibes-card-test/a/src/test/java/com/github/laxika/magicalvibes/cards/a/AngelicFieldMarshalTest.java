package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KembaKhaRegent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicFieldMarshal.class, GrizzlyBears.class, KembaKhaRegent.class})
class AngelicFieldMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant grants +2/+2 and vigilance while you control your commander")
    void lieutenantBonusesApplyWhileControllingCommander() {
        Card commander = new KembaKhaRegent();
        gd.makeCommander(player1.getId(), commander);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, commander);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant bonuses disappear when your commander leaves the battlefield")
    void lieutenantBonusesDisappearWhenCommanderLeaves() {
        Card commander = new KembaKhaRegent();
        gd.makeCommander(player1.getId(), commander);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A commander controlled by another player does not satisfy lieutenant")
    void opponentsCommanderDoesNotSatisfyLieutenant() {
        Card commander = new KembaKhaRegent();
        gd.makeCommander(player1.getId(), commander);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        harness.addToBattlefieldAndReturn(player2, commander);

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A different copy of your commander does not enable lieutenant")
    void anotherCopyOfCommanderDoesNotEnableLieutenant() {
        gd.makeCommander(player1.getId(), new KembaKhaRegent());
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        Permanent copy = harness.addToBattlefieldAndReturn(player1, new KembaKhaRegent());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Controlling another player's commander does not enable lieutenant")
    void controllingOpponentsCommanderDoesNotEnableLieutenant() {
        Card commander = new KembaKhaRegent();
        gd.makeCommander(player2.getId(), commander);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        Permanent stolenCommander = harness.addToBattlefieldAndReturn(player1, commander);

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, stolenCommander, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant grants vigilance to your commander and creatures entering later")
    void lieutenantAppliesToCommanderAndLaterCreatures() {
        Card commander = new KembaKhaRegent();
        gd.makeCommander(player1.getId(), commander);
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();

        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, commanderPermanent, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, commanderPermanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commanderPermanent)).isEqualTo(4);

        Permanent laterMarshal = harness.addToBattlefieldAndReturn(player1, new AngelicFieldMarshal());
        assertThat(gqs.hasKeyword(gd, laterMarshal, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterMarshal, Keyword.VIGILANCE)).isFalse();
    }
}
