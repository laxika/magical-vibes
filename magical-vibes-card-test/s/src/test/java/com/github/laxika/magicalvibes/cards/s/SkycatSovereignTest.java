package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkycatSovereign.class, GrizzlyBears.class, SuntailHawk.class})
class SkycatSovereignTest extends BaseCardTest {

    @Test
    void doesNotCountItselfOrOpponentsFlyingCreatures() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SkycatSovereign());
        harness.addToBattlefield(player2, new SkycatSovereign());

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(1);
    }

    @Test
    void multipleSovereignsCountEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkycatSovereign());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkycatSovereign());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSickAndCountsCreatedTokens() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SkycatSovereign());
        sovereign.setSummoningSick(true);
        sovereign.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for each other flying creature you control")
    void boostsForOtherFlyingCreaturesYouControl() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SkycatSovereign());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates a 1/1 white Cat Bird token with flying")
    void createsCatBirdToken() {
        harness.addToBattlefield(player1, new SkycatSovereign());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cat Bird");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.BIRD);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }
}
