package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinRabblemasterTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addRabblemaster(Player player) {
        Permanent rabblemaster = harness.addToBattlefieldAndReturn(player, new GoblinRabblemaster());
        rabblemaster.setSummoningSick(false);
        return rabblemaster;
    }

    private Permanent addGoblin(Player player) {
        Permanent piker = new Permanent(new GoblinPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(piker);
        return piker;
    }

    @Test
    @DisplayName("Creates a 1/1 red Goblin token with haste at the beginning of combat on your turn")
    void createsHastyGoblinAtBeginningOfCombat() {
        addRabblemaster(player1);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent goblin = tokens.getFirst();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creates no token at the beginning of combat on an opponent's turn")
    void noTokenOnOpponentsTurn() {
        addRabblemaster(player1);

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Gets +1/+0 for each other attacking Goblin")
    void boostScalesWithOtherAttackingGoblins() {
        Permanent rabblemaster = addRabblemaster(player1);
        addGoblin(player1);
        addGoblin(player1);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(rabblemaster.getPowerModifier()).isEqualTo(2);
        assertThat(rabblemaster.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking alone gives no boost (itself is not an 'other' Goblin)")
    void noBoostWhenAttackingAlone() {
        Permanent rabblemaster = addRabblemaster(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(rabblemaster.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Goblin attackers do not increase the boost")
    void nonGoblinAttackersNotCounted() {
        Permanent rabblemaster = addRabblemaster(player1);
        addGoblin(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(rabblemaster.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Goblin you control must attack")
    void otherGoblinYouControlMustAttack() {
        addRabblemaster(player1);
        addGoblin(player1);

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Goblin Rabblemaster itself is not forced to attack")
    void rabblemasterItselfNotForced() {
        Permanent rabblemaster = addRabblemaster(player1);

        beginDeclareAttackers(player1);
        gs.declareAttackers(gd, player1, List.of());

        assertThat(rabblemaster.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Goblin is not forced to attack")
    void opponentsGoblinNotForced() {
        addRabblemaster(player1);
        Permanent opponentGoblin = addGoblin(player2);

        beginDeclareAttackers(player2);
        gs.declareAttackers(gd, player2, List.of());

        assertThat(opponentGoblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A non-Goblin creature you control is not forced to attack")
    void nonGoblinNotForced() {
        addRabblemaster(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        beginDeclareAttackers(player1);
        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }
}
