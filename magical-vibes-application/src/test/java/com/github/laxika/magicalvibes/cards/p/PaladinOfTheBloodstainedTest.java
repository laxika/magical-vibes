package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaladinOfTheBloodstainedTest extends BaseCardTest {

    @Test
    @DisplayName("Paladin of the Bloodstained has one ON_ENTER_BATTLEFIELD token creation effect")
    void hasCorrectEffect() {
        PaladinOfTheBloodstained card = new PaladinOfTheBloodstained();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Vampire");
        assertThat(effect.power()).isEqualTo(1);
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.WHITE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(effect.keywords()).containsExactly(Keyword.LIFELINK);
        assertThat(effect.additionalTypes()).isEmpty();
    }

    @Test
    @DisplayName("Casting Paladin of the Bloodstained puts it on the battlefield and triggers ETB")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PaladinOfTheBloodstained()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paladin of the Bloodstained"));
    }

    @Test
    @DisplayName("When Paladin of the Bloodstained enters, a Vampire token is created")
    void etbCreatesVampireToken() {
        harness.setHand(player1, List.of(new PaladinOfTheBloodstained()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB token is a 1/1 white Vampire creature token with lifelink")
    void tokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new PaladinOfTheBloodstained()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Battlefield has both Paladin and Vampire token after ETB resolves")
    void battlefieldHasBothPermanents() {
        harness.setHand(player1, List.of(new PaladinOfTheBloodstained()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
