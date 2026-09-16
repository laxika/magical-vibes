package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.k.KirtarsDesire;
import com.github.laxika.magicalvibes.cards.k.KrosanArcher;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IridescentAngel.class, KirtarsDesire.class, KrosanArcher.class})
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

        harness.setHand(player1, List.of(createTargetedInstant("Colored Bolt", color,
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

        harness.setHand(player1, List.of(createTargetedInstant("Colorless Bolt", null, "{1}")));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(angel.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be enchanted by a white Aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent angel = addCreatureReady(player2, new IridescentAngel());

        harness.setHand(player1, List.of(new KirtarsDesire()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Cannot be blocked by a green creature with reach")
    void cannotBeBlockedByGreenCreatureWithReach() {
        Permanent angel = addCreatureReady(player1, new IridescentAngel());
        Permanent archer = addCreatureReady(player2, new KrosanArcher());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(archer),
                gd.playerBattlefields.get(player1.getId()).indexOf(angel)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Prevents combat damage from a green source")
    void preventsCombatDamageFromGreenSource() {
        Permanent angel = addCreatureReady(player1, new IridescentAngel());
        Permanent archer = addCreatureReady(player2, new KrosanArcher());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(angel),
                gd.playerBattlefields.get(player2.getId()).indexOf(archer))));
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Iridescent Angel");
        harness.assertNotOnBattlefield(player2, "Krosan Archer");
    }
}
