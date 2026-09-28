package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WastelandRaider.class, GrizzlyBears.class, HillGiant.class})
@DisplayName("Wasteland Raider")
class WastelandRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy per additional {2} paid")
    void squadCreatesTokenCopies() {
        castRaider(List.of("{2}", "{2}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wasteland Raider")).hasSize(3);
        resolveSacrificeChoices();
    }

    @Test
    @DisplayName("ETB makes each player choose a creature before sacrificing simultaneously")
    void eachPlayerChoosesCreatureBeforeSacrifice() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        castRaider(List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validIds()).contains(ownBear.getId(), ownGiant.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownGiant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownGiant.getId()));

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingBear.getId()));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player without a creature is skipped")
    void playerWithoutCreatureIsSkipped() {
        castRaider(List.of());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Wasteland Raider");
        harness.handleMultiplePermanentsChosen(player1, List.of(source.getId()));

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
