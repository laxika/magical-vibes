package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoiceOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Green creature cannot block Voice of Truth because it has flying")
    void greenCreatureCannotBlockBecauseOfFlying() {
        Permanent voice = addReadyCreature(player1, new VoiceOfTruth());
        voice.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("White creature cannot block Voice of Truth")
    void whiteCreatureCannotBlock() {
        Permanent voice = addReadyCreature(player1, new VoiceOfTruth());
        voice.setAttacking(true);
        Card blockerCard = createCreature("White Knight", 2, 2, CardColor.WHITE);
        blockerCard.setKeywords(Set.of(Keyword.FLYING));
        Permanent blocker = addReadyCreature(player2, blockerCard);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents white combat damage")
    void preventsWhiteCombatDamage() {
        Permanent attacker = addReadyCreature(player1, createCreature("White Knight", 3, 3, CardColor.WHITE));
        attacker.setAttacking(true);
        Permanent voice = addReadyCreature(player2, new VoiceOfTruth());
        voice.setBlocking(true);
        voice.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Voice of Truth");
    }

    @Test
    @DisplayName("Voice of Truth cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent voice = addReadyCreature(player2, new VoiceOfTruth());
        addReadyCreature(player2, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
        harness.setHand(player1, List.of(createTargetedInstant("White Bolt", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, voice.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
}
