package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PterodonKnightTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC ControlsSubtypeConditionalEffect(DINOSAUR) wrapping GrantKeywordEffect(FLYING, SELF)")
    void hasCorrectStaticEffect() {
        PterodonKnight card = new PterodonKnight();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);

        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect grant = (GrantKeywordEffect) conditional.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.FLYING);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional flying with Dinosaur =====

    @Test
    @DisplayName("Has flying when controller controls a Dinosaur")
    void hasFlyingWithDinosaur() {
        harness.addToBattlefield(player1, new PterodonKnight());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent knight = findPermanent(player1, "Pterodon Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("No flying without a Dinosaur")
    void noFlyingWithoutDinosaur() {
        harness.addToBattlefield(player1, new PterodonKnight());

        Permanent knight = findPermanent(player1, "Pterodon Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with a non-Dinosaur creature")
    void noFlyingWithNonDinosaurCreature() {
        harness.addToBattlefield(player1, new PterodonKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent knight = findPermanent(player1, "Pterodon Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    // ===== Loses flying when Dinosaur leaves =====

    @Test
    @DisplayName("Loses flying when Dinosaur leaves the battlefield")
    void losesFlyingWhenDinosaurLeaves() {
        harness.addToBattlefield(player1, new PterodonKnight());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent knight = findPermanent(player1, "Pterodon Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();

        // Remove the Dinosaur
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.DINOSAUR));

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    // ===== Opponent's Dinosaur doesn't count =====

    @Test
    @DisplayName("Opponent's Dinosaur does not grant flying")
    void opponentDinosaurDoesNotCount() {
        harness.addToBattlefield(player1, new PterodonKnight());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent knight = findPermanent(player1, "Pterodon Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    // ===== Helper methods =====

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }

}
