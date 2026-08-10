package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Burrowing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

class SongOfSerenityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack while Song of Serenity is on the battlefield")
    void enchantedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent enchanted = addReadyCreature(player1);
        attachBurrowing(enchanted, player2);

        beginAttack(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unenchanted creature can attack while Song of Serenity is on the battlefield")
    void unenchantedCreatureCanAttack() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        harness.setLife(player2, 20);
        Permanent unenchanted = addReadyCreature(player1);

        beginAttack(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(unenchanted);
        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enchanted creature cannot block while Song of Serenity is on the battlefield")
    void enchantedCreatureCannotBlock() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent enchanted = addReadyCreature(player2);
        attachBurrowing(enchanted, player1);

        beginBlock();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(enchanted);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unenchanted creature can block while Song of Serenity is on the battlefield")
    void unenchantedCreatureCanBlock() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2);

        beginBlock();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can attack after Song of Serenity leaves the battlefield")
    void restrictionLiftsWhenSongOfSerenityLeaves() {
        Permanent song = new Permanent(new SongOfSerenity());
        gd.playerBattlefields.get(player1.getId()).add(song);
        harness.setLife(player2, 20);
        Permanent enchanted = addReadyCreature(player1);
        attachBurrowing(enchanted, player2);

        gd.playerBattlefields.get(player1.getId()).remove(song);

        beginAttack(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreature(Player player) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void attachBurrowing(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void beginBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
