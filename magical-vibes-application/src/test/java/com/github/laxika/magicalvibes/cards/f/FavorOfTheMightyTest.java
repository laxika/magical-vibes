package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavorOfTheMightyTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createRedBolt() {
        Card card = new Card();
        card.setName("Red Bolt");
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void addFavor() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new FavorOfTheMighty()));
    }

    @Test
    @DisplayName("Creature with greatest mana value has protection and can't be targeted by a colored spell")
    void greatestManaValueIsProtected() {
        addFavor();
        Permanent big = addCreature(createCreature("Colossus", 6, 6, CardColor.GREEN, "{6}"));
        addCreature(createCreature("Runt", 1, 1, CardColor.GREEN, "{1}"));

        harness.setHand(player1, List.of(createRedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, big.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Creature without the greatest mana value is not protected")
    void lowerManaValueIsNotProtected() {
        addFavor();
        addCreature(createCreature("Colossus", 6, 6, CardColor.GREEN, "{6}"));
        Permanent runt = addCreature(createCreature("Runt", 1, 1, CardColor.GREEN, "{1}"));

        harness.setHand(player1, List.of(createRedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, runt.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Red Bolt");
    }

    @Test
    @DisplayName("Creatures tied for greatest mana value are all protected")
    void tiedGreatestManaValueAllProtected() {
        addFavor();
        addCreature(createCreature("Twin A", 5, 5, CardColor.GREEN, "{5}"));
        Permanent twinB = addCreature(createCreature("Twin B", 5, 5, CardColor.GREEN, "{5}"));
        // A lesser-mana-value creature is an unprotected legal target, so Red Bolt is castable
        // (CR 601.2c); targeting a tied-greatest twin is then rejected for protection.
        addCreature(createCreature("Runt", 1, 1, CardColor.GREEN, "{1}"));

        harness.setHand(player1, List.of(createRedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, twinB.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature loses protection once a larger creature is on the battlefield")
    void protectionRecomputesWhenLargerCreatureAppears() {
        addFavor();
        Permanent formerlyBiggest = addCreature(createCreature("Colossus", 6, 6, CardColor.GREEN, "{6}"));
        addCreature(createCreature("Titan", 8, 8, CardColor.GREEN, "{8}"));

        harness.setHand(player1, List.of(createRedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Colossus (MV 6) is no longer the greatest — Titan (MV 8) is — so it can be targeted.
        gs.playCard(gd, player1, 0, 0, formerlyBiggest.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
