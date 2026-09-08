package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightwindGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Nightwind Glider")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent glider = addReadyPermanent(player1, new NightwindGlider(), true);
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking Nightwind Glider")
    void protectionFromBlackPreventsBlocking() {
        Permanent glider = addReadyPermanent(player1, new NightwindGlider(), true);
        Permanent blocker = addReadyPermanent(player2, createFlyingCreature("Black Dragon", 3, 3, CardColor.BLACK), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addReadyPermanent(player1, createCreature("Black Knight", 3, 3, CardColor.BLACK), true);
        Permanent glider = addReadyPermanent(player2, new NightwindGlider(), false);
        glider.setBlocking(true);
        glider.addBlockingTarget(indexOf(player1, attacker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nightwind Glider");
    }

    @Test
    @DisplayName("Protection from black prevents targeting Nightwind Glider")
    void protectionFromBlackPreventsTargeting() {
        Permanent glider = addReadyPermanent(player2, new NightwindGlider(), false);
        addReadyPermanent(player2, new GrizzlyBears(), false);

        harness.setHand(player1, List.of(createTargetedInstant("Black Bolt", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, glider.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
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

    private static Card createFlyingCreature(String name, int power, int toughness, CardColor color) {
        Card card = createCreature(name, power, toughness, color);
        card.setKeywords(Set.of(Keyword.FLYING));
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

    private Permanent addReadyPermanent(Player player, Card card, boolean attacking) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
