package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlayersCleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped attacker must be blocked by an Eldrazi if one can block")
    void requiresEldraziBlockerIfAble() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addReadyCreature(player2);
        addReadyCreature(player2, CardSubtype.ELDRAZI);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("matching creature");
    }

    @Test
    @DisplayName("One Eldrazi blocker satisfies the requirement")
    void oneEldraziBlockerIsEnough() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addReadyCreature(player2);
        Permanent eldrazi = addReadyCreature(player2, CardSubtype.ELDRAZI);
        addReadyCreature(player2, CardSubtype.ELDRAZI);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(eldrazi.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No Eldrazi blocker is required when none can block")
    void noEldraziBlockerIsRequiredWhenNoneCanBlock() {
        Permanent attacker = addReadyCreature(player1);
        Permanent cleaver = addReadyCleaver(player1);
        cleaver.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent eldrazi = addReadyCreature(player2, CardSubtype.ELDRAZI);
        eldrazi.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(eldrazi.isBlocking()).isFalse();
    }

    private Permanent addReadyCleaver(Player player) {
        return addReady(player, new SlayersCleaver());
    }

    private Permanent addReadyCreature(Player player, CardSubtype... subtypes) {
        return addReady(player, createCreature(subtypes));
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card createCreature(CardSubtype... subtypes) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
