package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistDancer.class, CoralMerfolk.class})
class MistDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+0 and flying")
    void boostsOtherMerfolkYouControl() {
        harness.addToBattlefieldAndReturn(player1, new MistDancer());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Encore creates an untapped hasty copy and sacrifices it at the next end step")
    void encoreCreatesHastyTokenAndSacrificesIt() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MistDancer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mist Dancer")).hasSize(1);
        Permanent token = findPermanent(player1, "Mist Dancer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mist Dancer"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mist Dancer")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Mist Dancer excludes itself but two copies boost each other")
    void excludesItselfAndBoostsOtherCopies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MistDancer());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new MistDancer());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Postcombat encore creates a token outside combat without making it attack")
    void postcombatEncoreDoesNotCreateAnAttacker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MistDancer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Mist Dancer");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Mist Dancer");
    }

    @Test
    @DisplayName("Encore cannot be activated outside a main phase")
    void encoreRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new MistDancer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Mist Dancer")).isEmpty();
    }
}
