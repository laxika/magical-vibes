package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImperialLancerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC ControlsSubtypeConditionalEffect(DINOSAUR) wrapping GrantKeywordEffect(DOUBLE_STRIKE, SELF)")
    void hasCorrectStaticEffect() {
        ImperialLancer card = new ImperialLancer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);

        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect grant = (GrantKeywordEffect) conditional.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.DOUBLE_STRIKE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional double strike with Dinosaur =====

    @Test
    @DisplayName("Has double strike when controller controls a Dinosaur")
    void hasDoubleStrikeWithDinosaur() {
        harness.addToBattlefield(player1, new ImperialLancer());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent lancer = findPermanent(player1, "Imperial Lancer");
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No double strike without a Dinosaur")
    void noDoubleStrikeWithoutDinosaur() {
        harness.addToBattlefield(player1, new ImperialLancer());

        Permanent lancer = findPermanent(player1, "Imperial Lancer");
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No double strike with a non-Dinosaur creature")
    void noDoubleStrikeWithNonDinosaurCreature() {
        harness.addToBattlefield(player1, new ImperialLancer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent lancer = findPermanent(player1, "Imperial Lancer");
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Loses double strike when Dinosaur leaves =====

    @Test
    @DisplayName("Loses double strike when Dinosaur leaves the battlefield")
    void losesDoubleStrikeWhenDinosaurLeaves() {
        harness.addToBattlefield(player1, new ImperialLancer());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent lancer = findPermanent(player1, "Imperial Lancer");
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();

        // Remove the Dinosaur
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.DINOSAUR));

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Opponent's Dinosaur doesn't count =====

    @Test
    @DisplayName("Opponent's Dinosaur does not grant double strike")
    void opponentDinosaurDoesNotCount() {
        harness.addToBattlefield(player1, new ImperialLancer());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent lancer = findPermanent(player1, "Imperial Lancer");
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Helper methods =====

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
