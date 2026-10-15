package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WastelandRaider.class})
@DisplayName("Wasteland Raider")
class WastelandRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Squad copies each trigger a sacrifice, leaving no creatures when none were present")
    void squadCreatesTokenCopies() {
        castRaider(List.of("{2}", "{2}"));
        harness.passBothPriorities();
        resolveSacrificeChoices();
        harness.assertInGraveyard(player1, "Wasteland Raider");
        assertThat(findPermanents(player1, "Wasteland Raider")).isEmpty();
    }

    @Test
    @DisplayName("Paying squad creates separate squad and sacrifice triggers")
    void paidSquadAndSacrificeAreSeparateAbilities() {
        castRaider(List.of("{2}"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Two squad payments create two copies and each copy triggers one sacrifice")
    void squadCopiesRetainSacrificeAbilityWithoutCreatingMoreCopies() {
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new WastelandRaider());
        }
        castRaider(List.of("{2}", "{2}"));
        harness.passBothPriorities();
        resolveSacrificeChoices();

        assertThat(findPermanents(player1, "Wasteland Raider")).hasSize(4);
        assertThat(findPermanents(player1, "Wasteland Raider").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB makes each player choose a creature before sacrificing simultaneously")
    void eachPlayerChoosesCreatureBeforeSacrifice() {
        Permanent ownBear = addCreatureReady(player1, new WastelandRaider());
        Permanent ownGiant = addCreatureReady(player1, new WastelandRaider());
        Permanent opposingBear = addCreatureReady(player2, new WastelandRaider());
        addCreatureReady(player2, new WastelandRaider());
        castRaider(List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validIds()).contains(ownBear.getId(), ownGiant.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownGiant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownGiant.getId()));

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingBear.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGiant.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingBear.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear);
    }

    @Test
    @DisplayName("A player without a creature is skipped")
    void playerWithoutCreatureIsSkipped() {
        castRaider(List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wasteland Raider");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castRaider(List<String> repeatedAdditionalCosts) {
        harness.setHand(player1, List.of(new WastelandRaider()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castCreatureWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
    }

    private void resolveSacrificeChoices() {
        resolveAllTriggers();
        while (gd.interaction.isAwaitingInput()) {
            PendingInteraction.MultiPermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).isNotEmpty();
            if (choice.playerId().equals(player1.getId())) {
                harness.handleMultiplePermanentsChosen(player1, List.of(choice.validIds().getFirst()));
            } else {
                harness.handleMultiplePermanentsChosen(player2, List.of(choice.validIds().getFirst()));
            }
            resolveAllTriggers();
        }
    }
}
