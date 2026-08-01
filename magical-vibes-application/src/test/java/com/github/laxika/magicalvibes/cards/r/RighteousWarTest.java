package com.github.laxika.magicalvibes.cards.r;

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

class RighteousWarTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card createColoredBolt(CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(color.name() + " Bolt");
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addOwnCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void addRighteousWar() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new RighteousWar()));
    }

    @Test
    @DisplayName("White creature you control can't be targeted by a black spell")
    void whiteCreatureProtectedFromBlack() {
        addRighteousWar();
        Permanent white = addOwnCreature(createCreature("White Bear", CardColor.WHITE));
        // Unprotected alternate target keeps the bolt playable (CR 601.2c).
        addOwnCreature(createCreature("Green Bear", CardColor.GREEN));

        harness.setHand(player1, List.of(createColoredBolt(CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, white.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature you control can't be targeted by a white spell")
    void blackCreatureProtectedFromWhite() {
        addRighteousWar();
        Permanent black = addOwnCreature(createCreature("Black Bear", CardColor.BLACK));
        addOwnCreature(createCreature("Green Bear", CardColor.GREEN));

        harness.setHand(player1, List.of(createColoredBolt(CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, black.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("White creature remains targetable by a white spell")
    void whiteCreatureNotProtectedFromWhite() {
        addRighteousWar();
        Permanent white = addOwnCreature(createCreature("White Bear", CardColor.WHITE));

        harness.setHand(player1, List.of(createColoredBolt(CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, white.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Green creature you control is not protected")
    void greenCreatureNotProtected() {
        addRighteousWar();
        Permanent green = addOwnCreature(createCreature("Green Bear", CardColor.GREEN));

        harness.setHand(player1, List.of(createColoredBolt(CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, green.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's white creature is not protected")
    void opponentWhiteCreatureNotProtected() {
        addRighteousWar();
        Permanent oppWhite = new Permanent(createCreature("Opp White", CardColor.WHITE));
        oppWhite.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppWhite);

        harness.setHand(player1, List.of(createColoredBolt(CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, oppWhite.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
