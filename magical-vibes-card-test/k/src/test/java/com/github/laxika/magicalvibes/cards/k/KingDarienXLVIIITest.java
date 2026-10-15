package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingDarienXLVIII.class, GrizzlyBears.class})
class KingDarienXLVIIITest extends BaseCardTest {

    @Test
    @DisplayName("King Darien boosts other creatures you control")
    void boostsOtherCreaturesYouControl() {
        KingDarienXLVIII card = new KingDarienXLVIII();
        card.setPower(10);
        card.setToughness(10);
        Permanent king = addCreatureReady(player1, card);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability puts a counter on King Darien and creates a Soldier")
    void putsCounterOnKingAndCreatesSoldier() {
        Permanent king = addCreatureReady(player1, new KingDarienXLVIII());
        addKingDarienMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(king.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing King Darien protects your creature tokens until end of turn")
    void sacrificesAndProtectsCreatureTokens() {
        Permanent king = addCreatureReady(player1, new KingDarienXLVIII());
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());
        addKingDarienMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Soldier");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(king);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonToken, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonToken, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The Soldier is still created when King Darien is sacrificed before resolution")
    void createsSoldierAfterSourceIsSacrificed() {
        Permanent king = addCreatureReady(player1, new KingDarienXLVIII());
        addKingDarienMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(king);
        harness.assertInGraveyard(player1, "King Darien XLVIII");
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(king.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection applies only to your tokens present when the ability resolves")
    void protectionExcludesOpponentAndLaterTokens() {
        addCreatureReady(player1, new KingDarienXLVIII());
        addCreatureReady(player2, new KingDarienXLVIII());
        addKingDarienMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent protectedToken = findPermanent(player1, "Soldier");

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent opponentToken = findPermanent(player2, "Soldier");

        addKingDarienMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        Permanent laterToken = findPermanents(player1, "Soldier").stream()
                .filter(permanent -> permanent != protectedToken)
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, protectedToken, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectedToken, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterToken, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterToken, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, protectedToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, protectedToken)).isEqualTo(1);
    }

    private void addKingDarienMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
