package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnchainedBerserkerTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{W}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("Gets +2/+0 while attacking")
    void getsPlusTwoPlusZeroWhileAttacking() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());

        berserker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the attacking boost while not attacking")
    void noBoostWhileNotAttacking() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("White creature cannot block it")
    void whiteCreatureCannotBlock() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, createCreature("White Bear", 2, 2, CardColor.WHITE));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent berserker = addCreatureReady(player2, new UnchainedBerserker());
        addCreatureReady(player2, createCreature("Other Creature", 2, 2, CardColor.RED));

        harness.setHand(player1, List.of(createTargetedInstant("White Bolt", CardColor.WHITE)));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, berserker.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }
}
