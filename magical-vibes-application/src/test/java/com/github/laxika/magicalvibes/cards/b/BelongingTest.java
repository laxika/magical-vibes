package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed(Belonging.class)
class BelongingTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates three changeling Shapeshifter tokens")
    void entersAndCreatesShapeshifters() {
        harness.setHand(player1, List.of(new Belonging()));
        addManaForBelongingCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
            assertThat(token.getCard().getKeywords()).containsExactly(Keyword.CHANGELING);
        });
    }

    @Test
    @DisplayName("Encore creates a hasty token attacking each opponent and sacrifices it at the next end step")
    void encoreCreatesAttackingTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Belonging()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Belonging");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Belonging")).isEmpty();
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);
    }

    private void addManaForBelongingCast() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
