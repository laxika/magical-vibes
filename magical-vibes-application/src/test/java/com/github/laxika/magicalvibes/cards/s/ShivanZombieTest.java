package com.github.laxika.magicalvibes.cards.s;

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

class ShivanZombieTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor color, int power, int toughness) {
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
    @DisplayName("White creature cannot block Shivan Zombie")
    void whiteCreatureCannotBlock() {
        Permanent attacker = new Permanent(new ShivanZombie());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("White Knight", CardColor.WHITE, 2, 2));
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
    @DisplayName("White creature deals no combat damage to Shivan Zombie")
    void whiteCreatureDealsNoCombatDamage() {
        Permanent attacker = new Permanent(createCreature("White Knight", CardColor.WHITE, 3, 3));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new ShivanZombie());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Zombie");
    }

    @Test
    @DisplayName("Shivan Zombie cannot be targeted by a white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent zombie = new Permanent(new ShivanZombie());
        zombie.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(zombie);

        harness.setHand(player2, List.of(createTargetedInstant("White Bolt", CardColor.WHITE, "{W}")));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, zombie.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Shivan Zombie can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent zombie = new Permanent(new ShivanZombie());
        zombie.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(zombie);

        harness.setHand(player2, List.of(createTargetedInstant("Red Bolt", CardColor.RED, "{R}")));
        harness.addMana(player2, ManaColor.RED, 1);

        gs.playCard(gd, player2, 0, 0, zombie.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
