package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerixBladewingTest extends BaseCardTest {

    @Test
    @DisplayName("Has kicker {3} and kicked conditional ETB effect")
    void hasCorrectEffects() {
        VerixBladewing card = new VerixBladewing();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(KickerEffect.class);
        KickerEffect kicker = (KickerEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(kicker.cost()).isEqualTo("{3}");

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(KickedConditionalEffect.class);
        KickedConditionalEffect conditional =
                (KickedConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect = (CreateTokenEffect) conditional.wrapped();
        assertThat(tokenEffect.tokenName()).isEqualTo("Karox Bladewing");
        assertThat(tokenEffect.power()).isEqualTo(4);
        assertThat(tokenEffect.toughness()).isEqualTo(4);
        assertThat(tokenEffect.legendary()).isTrue();
    }

    @Test
    @DisplayName("Cast without kicker — enters as 4/4, no token created")
    void castWithoutKickerNoToken() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Verix Bladewing"));
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // Only Verix on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 7); // {2}{R}{R} + {3} kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Verix Bladewing"));
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — creates Karox Bladewing legendary 4/4 red Dragon token with flying")
    void castWithKickerCreatesKaroxToken() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Verix + Karox = 2 permanents
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        // Verify the Karox Bladewing token
        Permanent karox = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Karox Bladewing"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Karox Bladewing token not found"));

        assertThat(karox.getCard().getPower()).isEqualTo(4);
        assertThat(karox.getCard().getToughness()).isEqualTo(4);
        assertThat(karox.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(karox.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(karox.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(karox.getCard().isToken()).isTrue();
        assertThat(karox.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }
}
