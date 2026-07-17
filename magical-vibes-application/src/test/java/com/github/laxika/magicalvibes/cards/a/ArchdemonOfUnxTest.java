package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArchdemonOfUnxTest extends BaseCardTest {

    // "At the beginning of your upkeep, sacrifice a non-Zombie creature, then create a
    //  2/2 black Zombie creature token."

    private static Card zombieCreature() {
        Card card = new Card();
        card.setName("Test Zombie");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setSubtypes(List.of(CardSubtype.ZOMBIE));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private long blackZombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .filter(p -> p.getCard().getColor() == CardColor.BLACK)
                .filter(p -> p.getCard().getPower() == 2 && p.getCard().getToughness() == 2)
                .count();
    }

    @Test
    @DisplayName("With itself the only non-Zombie creature, it sacrifices itself and creates a 2/2 black Zombie")
    void sacrificesSelfAndCreatesToken() {
        Permanent archdemon = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice → Archdemon sacrificed
        harness.passBothPriorities(); // resolve token creation

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(archdemon.getId()));
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Zombie creature is not eligible to be sacrificed")
    void zombieCreatureIsNotSacrificed() {
        Permanent archdemon = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, zombieCreature());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice → only Archdemon is eligible, auto-sacrificed
        harness.passBothPriorities(); // resolve token creation

        // The pre-existing Zombie survives (it was never a legal sacrifice)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(zombie.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(archdemon.getId()));
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("With multiple non-Zombie creatures, the controller chooses which one to sacrifice")
    void controllerChoosesWhichNonZombieToSacrifice() {
        Permanent archdemon = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        // The upkeep puts two independent triggers on the stack (sacrifice, create token) whose
        // relative order isn't fixed; resolve priorities until the sacrifice prompt appears.
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (choice == null) {
            harness.passBothPriorities();
            choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        }
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve any remaining trigger (token creation)

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(archdemon.getId()));
        assertThat(blackZombieTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent archdemon = harness.addToBattlefieldAndReturn(player1, new ArchdemonOfUnx());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(archdemon.getId()));
        assertThat(blackZombieTokens()).isEqualTo(0);
    }
}
