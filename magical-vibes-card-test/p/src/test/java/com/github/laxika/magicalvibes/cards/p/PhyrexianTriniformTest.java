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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianTriniform.class, WrathOfGod.class})
class PhyrexianTriniformTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Phyrexian Triniform creates three Phyrexian Golems")
    void deathCreatesThreePhyrexianGolems() {
        addCreatureReady(player1, new PhyrexianTriniform());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
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
    @DisplayName("Encore creates an untapped copy and its end-step sacrifice creates three Golems")
    void encoreCreatesCopyAndDeathTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent tokenCopy = findPermanent(player1, "Phyrexian Triniform");
        assertThat(tokenCopy.getCard().isToken()).isTrue();
        assertThat(tokenCopy.isTapped()).isFalse();
        assertThat(tokenCopy.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, tokenCopy, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Triniform")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3);
    }

    @Test
    @DisplayName("Encore paid after combat creates a nonattacking copy that is still sacrificed")
    void encoreAfterCombatDoesNotEnterAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent copy = findPermanent(player1, "Phyrexian Triniform");
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Triniform")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3);
    }

    @Test
    @DisplayName("Encore exiles the source as an activation cost before creating its copy")
    void encoreExilesSourceBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Phyrexian Triniform"));
        assertThat(findPermanents(player1, "Phyrexian Triniform")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Triniform")).hasSize(1);
    }

    @Test
    @DisplayName("An able Encore copy must be declared as an attacker")
    void encoreCopyCannotSkipAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore cannot be activated during upkeep")
    void encoreRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new PhyrexianTriniform()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Triniform")).isEmpty();
    }
}
