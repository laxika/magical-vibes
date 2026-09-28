package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianTriniform.class, WrathOfGod.class})
class PhyrexianTriniformTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Phyrexian Triniform creates three Phyrexian Golems")
    void deathCreatesThreePhyrexianGolems() {
        addCreatureReady(player1, new PhyrexianTriniform());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
            assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Encore creates an attacking copy and its death trigger creates three Golems")
    void encoreCreatesCopyAndDeathTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent tokenCopy = findPermanent(player1, "Phyrexian Triniform");
        assertThat(tokenCopy.getCard().isToken()).isTrue();
        assertThat(tokenCopy.isTapped()).isTrue();
        assertThat(tokenCopy.isAttacking()).isTrue();
        assertThat(tokenCopy.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(tokenCopy.getCard().getKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Triniform")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3);
    }
}
