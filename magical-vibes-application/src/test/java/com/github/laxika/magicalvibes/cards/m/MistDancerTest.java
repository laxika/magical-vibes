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
    @DisplayName("Encore creates a hasty token copy attacking each opponent and sacrifices it at the next end step")
    void encoreCreatesAttackingTokenAndSacrificesIt() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MistDancer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Mist Dancer").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mist Dancer"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mist Dancer")).isEmpty();
    }
}
