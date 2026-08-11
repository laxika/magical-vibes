package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IridescentAngelTest extends BaseCardTest {

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @ParameterizedTest(name = "Cannot be targeted by {0} instant")
    @EnumSource(CardColor.class)
    void cannotBeTargetedByColoredInstant(CardColor color) {
        Permanent angel = addCreatureReady(player2, new IridescentAngel());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(createTargetedInstant("Colored Bolt", color,
                "{" + color.getCode() + "}")));
        harness.addMana(player1, ManaColor.valueOf(color.name()), 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, angel.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from " + color.name().toLowerCase());
    }

    @Test
    @DisplayName("Can be targeted by a colorless instant")
    void canBeTargetedByColorlessInstant() {
        Permanent angel = addCreatureReady(player2, new IridescentAngel());

        harness.setHand(player1, java.util.List.of(createTargetedInstant("Colorless Bolt", null, "{1}")));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(1);
    }
}
