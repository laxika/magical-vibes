package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XathridDemonTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Xathrid Demon has correct upkeep trigger effect")
    void hasCorrectEffect() {
        XathridDemon card = new XathridDemon();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect.class);
    }

    // ===== No other creatures — tap and lose 7 life =====

    @Test
    @DisplayName("Taps and controller loses 7 life when no other creatures are present")
    void tapAndLose7LifeWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("Xathrid Demon is tapped when no other creatures")
    void demonIsTappedWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Xathrid Demon"))
                .findFirst().orElseThrow();
        assertThat(demon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent does not lose life when controller can't sacrifice")
    void opponentUnaffectedWhenNoSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    // ===== One other creature — auto-sacrifice, opponent loses life =====

    @Test
    @DisplayName("Auto-sacrifices the only other creature")
    void autoSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears()); // 2/2

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent loses life equal to sacrificed creature's power (auto-sacrifice)")
    void opponentLosesLifeEqualToPowerAutoSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears()); // 2/2
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Controller does not lose life when sacrifice succeeds")
    void controllerDoesNotLoseLifeOnSuccessfulSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Xathrid Demon is not tapped when sacrifice succeeds")
    void demonNotTappedOnSuccessfulSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Xathrid Demon"))
                .findFirst().orElseThrow();
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Xathrid Demon remains on the battlefield after sacrificing another creature")
    void demonRemainsAfterSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Xathrid Demon"));
    }

    @Test
    @DisplayName("Opponent loses life equal to bigger creature's power")
    void opponentLosesLifeEqualToBiggerCreaturePower() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GiantSpider()); // 2/4
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    // ===== Multiple other creatures — player chooses =====

    @Test
    @DisplayName("Prompts player to choose when multiple other creatures are present")
    void promptsChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent spider = addCreature(player1, new GiantSpider());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife.class);
        assertThat(gd.interaction.permanentChoice().validIds()).contains(bears.getId(), spider.getId());
    }

    @Test
    @DisplayName("Xathrid Demon itself is not in the valid sacrifice choices")
    void demonNotInValidChoices() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player1, new GrizzlyBears());
        addCreature(player1, new GiantSpider());

        Permanent demonPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Xathrid Demon"))
                .findFirst().orElseThrow();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(demonPerm.getId());
    }

    @Test
    @DisplayName("Player chooses creature to sacrifice, opponent loses life equal to its power")
    void playerChoosesCreatureOpponentLosesLife() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreature(player1, new GrizzlyBears()); // 2/2
        addCreature(player1, new GiantSpider()); // 2/4
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Opponent loses life equal to Grizzly Bears' power (2)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new XathridDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Opponent's creatures are not valid sacrifice targets")
    void opponentCreaturesNotValidTargets() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreature(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Controller loses 7 life and demon is tapped (no OTHER creatures controller owns)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
        // Opponent's creature is untouched, opponent doesn't lose life from sacrifice
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Life loss at low life can kill controller (game ends)")
    void lifeLossAtLowLifeEndsGame() {
        harness.addToBattlefield(player1, new XathridDemon());
        harness.setLife(player1, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3 - 7);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life loss is not damage — cannot be prevented by color-based damage prevention")
    void lifeLossNotPreventedByColorPrevention() {
        harness.addToBattlefield(player1, new XathridDemon());
        gd.preventDamageFromColors.add(com.github.laxika.magicalvibes.model.CardColor.BLACK);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Life loss is NOT damage, so it is NOT prevented
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }
}
