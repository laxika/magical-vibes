package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MelesseSpiritTest extends BaseCardTest {

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

    @Test
    @DisplayName("Black flying creature cannot block Melesse Spirit")
    void blackCreatureCannotBlock() {
        Permanent attacker = new Permanent(new MelesseSpirit());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card blackFlyer = createCreature("Black Beast", 2, 2, CardColor.BLACK);
        blackFlyer.setKeywords(EnumSet.of(Keyword.FLYING));
        Permanent blocker = new Permanent(blackFlyer);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackInstant() {
        Permanent spirit = new Permanent(new MelesseSpirit());
        spirit.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spirit);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Black Removal", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, spirit.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a white spell")
    void canBeTargetedByWhiteInstant() {
        Permanent spirit = new Permanent(new MelesseSpirit());
        spirit.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        harness.setHand(player1, List.of(createTargetedInstant("White Blast", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, spirit.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("White Blast");
    }
}
