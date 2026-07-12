package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmberGaleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each white and/or blue creature the target player controls")
    void damagesWhiteAndBlueCreatures() {
        Permanent white = addCreature(player2, makeCreature("Soldier", 2, 2, CardColor.WHITE));
        Permanent blue = addCreature(player2, makeCreature("Merfolk", 2, 2, CardColor.BLUE));

        castEmberGale();

        assertThat(white.getMarkedDamage()).isEqualTo(1);
        assertThat(blue.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage creatures that are neither white nor blue")
    void doesNotDamageOtherColors() {
        Permanent red = addCreature(player2, makeCreature("Goblin", 2, 2, CardColor.RED));

        castEmberGale();

        assertThat(red.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage the caster's own white creatures")
    void doesNotDamageCastersCreatures() {
        Permanent ownWhite = addCreature(player1, makeCreature("Soldier", 2, 2, CardColor.WHITE));

        castEmberGale();

        assertThat(ownWhite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kills a 1-toughness white creature")
    void killsSmallWhiteCreature() {
        addCreature(player2, makeCreature("Squire", 1, 1, CardColor.WHITE));

        castEmberGale();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Squire"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Squire"));
    }

    @Test
    @DisplayName("Target player's creatures can't block this turn")
    void creaturesCantBlock() {
        Permanent white = addCreature(player2, makeCreature("Soldier", 2, 2, CardColor.WHITE));
        Permanent red = addCreature(player2, makeCreature("Goblin", 2, 2, CardColor.RED));

        castEmberGale();

        assertThat(white.isCantBlockThisTurn()).isTrue();
        assertThat(red.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can't-block prevents declaring blockers")
    void preventsDeclaringBlockers() {
        Permanent attacker = addCreature(player1, makeCreature("Bear", 2, 2, CardColor.RED));
        addCreature(player2, makeCreature("Wall", 0, 4, CardColor.BLUE));

        castEmberGale();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castEmberGale() {
        harness.setHand(player1, List.of(new EmberGale()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card makeCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
