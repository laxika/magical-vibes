package com.github.laxika.magicalvibes.cards.o;

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

class OraxidTest extends BaseCardTest {

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
    @DisplayName("Oraxid cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent oraxid = new Permanent(new Oraxid());
        oraxid.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oraxid);

        Permanent validTarget = new Permanent(createCreature("Other Creature", 1, 1, CardColor.BLUE));
        validTarget.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(validTarget);

        harness.setHand(player1, List.of(createTargetedInstant("Red Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, oraxid.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Oraxid can be targeted by a non-red instant")
    void canBeTargetedByNonRedInstant() {
        Permanent oraxid = new Permanent(new Oraxid());
        oraxid.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oraxid);

        harness.setHand(player1, List.of(createTargetedInstant("Blue Bolt", CardColor.BLUE, "{U}")));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, oraxid.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Oraxid is not dealt combat damage by a red creature")
    void redCombatDamageIsPrevented() {
        Permanent attacker = new Permanent(createCreature("Red Creature", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent oraxid = new Permanent(new Oraxid());
        oraxid.setSummoningSick(false);
        oraxid.setBlocking(true);
        oraxid.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(oraxid);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Oraxid");
    }
}
