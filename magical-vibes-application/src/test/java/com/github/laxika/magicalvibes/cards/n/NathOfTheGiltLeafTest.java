package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NathOfTheGiltLeafTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private long elfWarriorTokens(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .count();
    }

    // ===== Upkeep trigger targeting =====

    @Test
    @DisplayName("Upkeep trigger only offers opponents as valid targets")
    void upkeepTargetFilterExcludesController() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    // ===== Full combo: discard triggers the token =====

    @Test
    @DisplayName("Accepting the discard makes the opponent discard at random and creates an Elf Warrior token")
    void discardTriggersTokenCreation() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId()); // target opponent
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt for discard
        harness.handleMayAbilityChosen(player1, true); // opponent discards at random → token trigger on stack
        harness.passBothPriorities(); // resolve token trigger → may prompt for token
        harness.handleMayAbilityChosen(player1, true); // create the Elf Warrior token
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("at random"));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(1)
                .allMatch(p -> p.getCard().getPower() == 1 && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.ELF)
                        && p.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    // ===== Declining the token after the discard =====

    @Test
    @DisplayName("Declining the token trigger leaves the discard in place but no token")
    void discardWithoutTokenWhenDeclined() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // discard → token trigger on stack
        harness.passBothPriorities(); // resolve token trigger → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline token
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(elfWarriorTokens(player1)).isZero();
    }

    // ===== Declining the discard =====

    @Test
    @DisplayName("Declining the discard leaves the opponent's hand intact and makes no token")
    void decliningDiscardDoesNothing() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline discard
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isZero();
    }

    // ===== Only fires on controller's upkeep =====

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isZero();
    }
}
