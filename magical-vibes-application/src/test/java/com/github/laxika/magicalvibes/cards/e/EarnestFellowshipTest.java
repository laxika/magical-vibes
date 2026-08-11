package com.github.laxika.magicalvibes.cards.e;

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

class EarnestFellowshipTest extends BaseCardTest {

    @Test
    @DisplayName("A creature cannot block a creature sharing its color")
    void coloredCreatureCannotBlockSameColorCreature() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent attacker = addReadyCreature(player1, "White attacker", List.of(CardColor.WHITE));
        attacker.setAttacking(true);
        addReadyCreature(player2, "White blocker", List.of(CardColor.WHITE));

        prepareDeclareBlockers(player1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A colorless creature can block a colored creature")
    void colorlessCreatureCanBlockColoredCreature() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent attacker = addReadyCreature(player1, "White attacker", List.of(CardColor.WHITE));
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, "Colorless blocker", List.of());

        prepareDeclareBlockers(player1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A multicolored creature cannot be targeted by spells of either color")
    void multicoloredCreatureHasProtectionFromEachColor() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent target = addReadyCreature(player2, "Azorius creature", List.of(CardColor.WHITE, CardColor.BLUE));
        addReadyCreature(player2, "Other target", List.of(CardColor.BLACK));

        harness.setHand(player1, List.of(createTargetedInstant("Blue Bolt", CardColor.BLUE)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");

        harness.setHand(player1, List.of(createTargetedInstant("White Bolt", CardColor.WHITE)));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       String name, List<CardColor> colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(colors.size() == 1 ? colors.getFirst() : null);
        card.setColors(colors);
        card.setPower(2);
        card.setToughness(2);
        return addCreatureReady(player, card);
    }

    private static Card createTargetedInstant(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setColors(List.of(color));
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }
}
