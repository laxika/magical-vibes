package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LordOfThePitTest extends BaseCardTest {

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

    // ===== No other creatures — deals 7 damage =====

    @Test
    @DisplayName("Deals 7 damage to controller when no other creatures are present")
    void deals7DamageWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new LordOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("Deals 7 damage log message when no other creatures")
    void logsDamageWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new LordOfThePit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Lord of the Pit deals 7 damage"));
    }

    @Test
    @DisplayName("Does not deal damage to opponent when controller has no other creatures")
    void doesNotDealDamageToOpponent() {
        harness.addToBattlefield(player1, new LordOfThePit());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    // ===== One other creature — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices the only other creature")
    void autoSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new LordOfThePit());
        Permanent bears = addCreature(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Lord of the Pit remains on the battlefield after sacrificing another creature")
    void lordRemainsAfterSacrifice() {
        harness.addToBattlefield(player1, new LordOfThePit());
        addCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lord of the Pit"));
    }

    // ===== Multiple other creatures — player chooses =====

    @Test
    @DisplayName("Prompts player to choose when multiple other creatures are present")
    void promptsChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new LordOfThePit());
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent spider = addCreature(player1, new GiantSpider());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(bears.getId(), spider.getId());
    }

    @Test
    @DisplayName("Lord of the Pit itself is not in the valid sacrifice choices")
    void lordNotInValidChoices() {
        harness.addToBattlefield(player1, new LordOfThePit());
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent spider = addCreature(player1, new GiantSpider());

        Permanent lordPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of the Pit"))
                .findFirst().orElseThrow();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(lordPerm.getId());
    }

    @Test
    @DisplayName("Player chooses which creature to sacrifice")
    void playerChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new LordOfThePit());
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent spider = addCreature(player1, new GiantSpider());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lord of the Pit"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // No damage dealt when sacrifice succeeds
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new LordOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // No trigger — no damage, no sacrifice prompt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Two Lords of the Pit — each trigger sacrifices the other Lord, no damage dealt")
    void twoLordsEachSacrificesTheOther() {
        harness.addToBattlefield(player1, new LordOfThePit());
        harness.addToBattlefield(player1, new LordOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);

        // Two triggers on the stack
        assertThat(gd.stack).hasSize(2);

        // First trigger resolves — auto-sacrifices the other Lord
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Lord of the Pit")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lord of the Pit"));

        // Second trigger's source is already gone; the remaining Lord is a different
        // card so it's not excluded by "other than" — gets auto-sacrificed too
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Lord of the Pit")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Lord of the Pit"));
        // No damage dealt — both triggers found a creature to sacrifice
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's creatures are not valid sacrifice targets")
    void opponentCreaturesNotValidTargets() {
        harness.addToBattlefield(player1, new LordOfThePit());
        Permanent opponentCreature = addCreature(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Lord sees no OTHER creatures controller owns — deals damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
        // Opponent's creature is untouched
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Damage is prevented when color-based damage prevention is active")
    void damagePreventedByColorPrevention() {
        harness.addToBattlefield(player1, new LordOfThePit());
        gd.preventDamageFromColors.add(CardColor.BLACK);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Damage from black source is prevented
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("is prevented"));
    }

    @Test
    @DisplayName("Damage at low life can kill controller (game ends)")
    void damageAtLowLifeEndsGame() {
        harness.addToBattlefield(player1, new LordOfThePit());
        harness.setLife(player1, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3 - 7);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

