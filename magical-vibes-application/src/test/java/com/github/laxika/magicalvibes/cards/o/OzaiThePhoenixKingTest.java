package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonestWork;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OzaiThePhoenixKing.class, GrizzlyBears.class, Shock.class, HonestWork.class})
class OzaiThePhoenixKingTest extends BaseCardTest {

    @Test
    void preservesUnspentManaAsRed() {
        addOzai();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void gainsFlyingAndIndestructibleAtSixUnspentMana() {
        Permanent ozai = addOzai();
        harness.addMana(player1, ManaColor.RED, 6);

        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void losesConditionalKeywordsWhenManaFallsBelowSix() {
        Permanent ozai = addOzai();
        harness.addMana(player1, ManaColor.RED, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new Shock()));

        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isTrue();
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void firebendingAddsFourRedManaAndOzaiPreservesItAfterCombat() {
        addOzai();

        declareAttackers(java.util.List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void conditionalKeywordsCountManaOfEveryColorIncludingColorless() {
        Permanent ozai = addOzai();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void doesNotUseOrPreserveOpponentsMana() {
        Permanent ozai = addOzai();
        harness.addMana(player2, ManaColor.RED, 6);

        assertThat(gqs.hasKeyword(gd, ozai, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ozai, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void manaReplacementStopsWhenOzaiLosesItsAbilities() {
        Permanent ozai = addOzai();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, java.util.List.of(new HonestWork()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, ozai.getId());
        resolveAllTriggers();
        assertThat(gqs.hasLostAllAbilities(gd, ozai)).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private Permanent addOzai() {
        return harness.addToBattlefieldAndReturn(player1, new OzaiThePhoenixKing());
    }
}
