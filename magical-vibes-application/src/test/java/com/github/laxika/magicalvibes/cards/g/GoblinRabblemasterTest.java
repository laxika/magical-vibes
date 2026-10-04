package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinRabblemaster.class, GoblinRoughrider.class, RuneclawBear.class, LightningStrike.class})
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
        return addCreatureReady(player, new GoblinRabblemaster());
    }

    private Permanent addGoblin(Player player) {
        return addCreatureReady(player, new GoblinRoughrider());
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
        addCreatureReady(player1, new RuneclawBear());

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
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        beginDeclareAttackers(player1);
        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The newly created hasty Goblin must attack and increases Rabblemaster's bonus")
    void tokenMustAttackAndContributesToBonus() {
        Permanent rabblemaster = addRabblemaster(player1);
        advanceToCombat(player1);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(rabblemaster.getPowerModifier()).isEqualTo(1);
        assertThat(rabblemaster.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Tapped and summoning-sick Goblins are not forced to attack or counted")
    void unableGoblinsAreNotForcedOrCounted() {
        Permanent rabblemaster = addRabblemaster(player1);
        Permanent tappedGoblin = addGoblin(player1);
        tappedGoblin.setTapped(true);
        Permanent sickGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinRoughrider());
        sickGoblin.setSummoningSick(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(tappedGoblin.isAttacking()).isFalse();
        assertThat(sickGoblin.isAttacking()).isFalse();
        assertThat(rabblemaster.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Two Rabblemasters force each other to attack and each counts the other")
    void twoRabblemastersForceEachOtherToAttack() {
        Permanent first = addRabblemaster(player1);
        Permanent second = addRabblemaster(player1);

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Goblin destroyed in response is not counted when the attack trigger resolves")
    void countsAttackingGoblinsAtResolution() {
        Permanent rabblemaster = addRabblemaster(player1);
        Permanent goblin = addGoblin(player1);
        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, goblin.getId());
        harness.assertInGraveyard(player1, "Goblin Roughrider");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(rabblemaster.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The bonus remains fixed when another attacking Goblin subsequently dies")
    void resolvedBonusRemainsFixed() {
        Permanent rabblemaster = addRabblemaster(player1);
        Permanent goblin = addGoblin(player1);
        declareAttackers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(rabblemaster.getPowerModifier()).isEqualTo(1);

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, goblin.getId());

        harness.assertInGraveyard(player1, "Goblin Roughrider");
        assertThat(rabblemaster.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Rabblemaster in response does not stop its combat token trigger")
    void combatTokenTriggerSurvivesSourceRemoval() {
        Permanent rabblemaster = addRabblemaster(player1);
        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, rabblemaster.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Goblin Rabblemaster");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("The attack bonus lasts through combat and expires at cleanup")
    void bonusExpiresAtEndOfTurn() {
        Permanent rabblemaster = addRabblemaster(player1);
        addGoblin(player1);
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(rabblemaster.getPowerModifier()).isEqualTo(1);
        harness.passUntil(player1, TurnStep.CLEANUP);
        assertThat(rabblemaster.getPowerModifier()).isZero();
        assertThat(rabblemaster.getToughnessModifier()).isZero();
    }
}
