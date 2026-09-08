package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandOfHonorTest extends BaseCardTest {

    @Test
    @DisplayName("When Hand of Honor becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent hand = addReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Hand of Honor blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent hand = addReady(player2, new HandOfHonor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creature cannot block Hand of Honor")
    void blackCreatureCannotBlock() {
        Permanent hand = addReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        Permanent blocker = addReady(player2, createCreature("Black Creature", 2, 2, CardColor.BLACK));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Black creature deals no combat damage to Hand of Honor")
    void takesNoDamageFromBlackCreature() {
        Permanent attacker = addReady(player1, createCreature("Black Creature", 3, 3, CardColor.BLACK));
        attacker.setAttacking(true);
        Permanent hand = addReady(player2, new HandOfHonor());
        hand.setBlocking(true);
        hand.addBlockingTarget(0);

        prepareDeclareBlockers();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hand);
    }

    @Test
    @DisplayName("Black instant cannot target Hand of Honor")
    void cannotBeTargetedByBlackInstant() {
        Permanent hand = addReady(player2, new HandOfHonor());
        addReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(createTargetedInstant("Black Bolt", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, hand.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Red instant can target Hand of Honor")
    void canBeTargetedByRedInstant() {
        Permanent hand = addReady(player1, new HandOfHonor());

        harness.setHand(player1, List.of(createTargetedInstant("Red Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, hand.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Red Bolt");
    }

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

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
