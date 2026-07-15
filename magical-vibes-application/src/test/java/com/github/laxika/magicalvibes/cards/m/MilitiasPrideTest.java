package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MilitiasPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken attacker: pay {W} creates a tapped, attacking 1/1 Kithkin Soldier")
    void payCreatesTappedAttackingToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, nontokenCreature());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // The token entered tapped and attacking. handleMayAbilityChosen auto-passes past
        // END_OF_COMBAT, which clears isAttacking; assert attackedThisTurn + the game log instead.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.KITHKIN)
                        && p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(p.isTapped()).isTrue();
                    assertThat(p.isAttackedThisTurn()).isTrue();
                });

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Kithkin Soldier") && log.contains("tapped and attacking"));
    }

    @Test
    @DisplayName("Declining the may-pay creates no token")
    void declineCreatesNoToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, nontokenCreature());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("A token creature attacking does not trigger the ability")
    void tokenAttackerDoesNotTrigger() {
        addCreatureReady(player1, new MilitiasPride());
        Permanent tokenAttacker = addCreatureReady(player1, nontokenCreature());
        TestCards.mutableCard(tokenAttacker).setToken(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with insufficient mana creates no token")
    void insufficientManaCreatesNoToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, nontokenCreature());
        // No mana added.

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    private Card nontokenCreature() {
        Card creature = new Card();
        creature.setName("Bear");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of(CardSubtype.BEAR));
        creature.setPower(2);
        creature.setToughness(2);
        creature.setManaCost("{1}{G}");
        creature.setColor(CardColor.GREEN);
        return creature;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
