package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostMonarchOfIfnir.class, WalkingCorpse.class, GrizzlyBears.class, Forest.class})
class LostMonarchOfIfnirTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control gain afflict 3")
    void otherZombiesGainAfflictThree() {
        addCreatureReady(player1, new LostMonarchOfIfnir());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new GrizzlyBears());
        setAttacking(zombie);

        beginBlockersAndAssign(0, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Zombie combat damage mills three cards and offers a creature return")
    void zombieCombatDamageMillsAndOffersReturn() {
        addCreatureReady(player1, new LostMonarchOfIfnir());
        addCreatureReady(player1, new WalkingCorpse());
        Card returnedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(returnedCreature));

        declareAttackers(List.of(1));
        resolveCombat();
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int creatureIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId()
                        .equals(returnedCreature.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, creatureIndex);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-Zombie combat damage does not trigger the second-main ability")
    void nonZombieCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new LostMonarchOfIfnir());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(1));
        resolveCombat();
        advanceToPostcombatMain(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Lost Monarch of Ifnir has afflict 3")
    void selfHasAfflictThree() {
        Permanent monarch = addCreatureReady(player1, new LostMonarchOfIfnir());
        addCreatureReady(player2, new GrizzlyBears());
        setAttacking(monarch);

        beginBlockersAndAssign(0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void setAttacking(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void beginBlockersAndAssign(int blockerIndex, int attackerIndex) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
