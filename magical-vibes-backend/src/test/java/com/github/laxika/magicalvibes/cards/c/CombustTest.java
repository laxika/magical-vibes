package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombustTest extends BaseCardTest {

    private static Card createWhiteCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createBlueCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createRedCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Has correct effects — unpreventable DealDamageToTargetCreatureEffect and cant be countered")
    void hasCorrectEffects() {
        Combust card = new Combust();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeCounteredEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect effect = (DealDamageToTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(5);
        assertThat(effect.unpreventable()).isTrue();
    }

    @Test
    @DisplayName("Deals 5 damage to target white creature and kills it")
    void deals5DamageToWhiteCreature() {
        harness.addToBattlefield(player2, createWhiteCreature("White Knight", 2, 2));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "White Knight");
        harness.assertInGraveyard(player2, "White Knight");
    }

    @Test
    @DisplayName("Deals 5 damage to target blue creature and kills it")
    void deals5DamageToBlueCreature() {
        harness.addToBattlefield(player2, createBlueCreature("Azure Drake", 2, 4));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Azure Drake");
    }

    @Test
    @DisplayName("Cannot target non-white non-blue creature")
    void cannotTargetRedCreature() {
        harness.addToBattlefield(player2, createWhiteCreature("Valid Target", 1, 1)); // valid target so spell is playable
        harness.addToBattlefield(player2, createRedCreature("Goblin Piker", 2, 1));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");

        // Engine rejects invalid targets with an exception
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or blue creature");
    }

    @Test
    @DisplayName("Damage cannot be prevented by prevention shield")
    void damageCannotBePreventedByShield() {
        harness.addToBattlefield(player2, createWhiteCreature("Shielded Angel", 2, 4));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Give the creature a damage prevention shield
        UUID targetId = harness.getPermanentId(player2, "Shielded Angel");
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shielded Angel"))
                .findFirst().orElseThrow();
        target.setDamagePreventionShield(10);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Creature should still die despite prevention shield
        harness.assertNotOnBattlefield(player2, "Shielded Angel");
        harness.assertInGraveyard(player2, "Shielded Angel");
    }

    @Test
    @DisplayName("Large creature survives 5 unpreventable damage")
    void largeCreatureSurvives() {
        harness.addToBattlefield(player2, createWhiteCreature("Serra Angel", 4, 6));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Serra Angel is 4/6, survives 5 damage
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, createWhiteCreature("White Knight", 2, 2));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Combust");
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, createWhiteCreature("White Knight", 2, 2));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Combust");
    }
}
