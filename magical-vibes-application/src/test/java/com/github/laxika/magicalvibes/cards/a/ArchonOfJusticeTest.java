package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.cards.p.PunctureBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfJustice.class, CascadeBluffs.class, PunctureBlast.class})
class ArchonOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("When Archon of Justice dies, exile target permanent (a land)")
    void diesExilesTargetLand() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player2, new CascadeBluffs());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new PunctureBlast(), new PunctureBlast()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID cascadeBluffsId = harness.getPermanentId(player2, "Cascade Bluffs");

        killArchonWithPunctureBlasts(archonId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, cascadeBluffsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cascade Bluffs");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cascade Bluffs"));
    }

    @Test
    @DisplayName("Death trigger can target a permanent controlled by Archon of Justice's controller")
    void canTargetControllerPermanent() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player1, new CascadeBluffs());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new PunctureBlast(), new PunctureBlast()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID cascadeBluffsId = harness.getPermanentId(player1, "Cascade Bluffs");

        killArchonWithPunctureBlasts(archonId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, cascadeBluffsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cascade Bluffs");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cascade Bluffs"));
    }

    @Test
    @DisplayName("Death trigger can target any permanent, including non-creatures")
    void targetFilterAllowsAnyPermanent() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player2, new CascadeBluffs());
        harness.addToBattlefield(player2, new ArchonOfJustice());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new PunctureBlast(), new PunctureBlast()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID cascadeBluffsId = harness.getPermanentId(player2, "Cascade Bluffs");
        UUID opposingArchonId = harness.getPermanentId(player2, "Archon of Justice");

        killArchonWithPunctureBlasts(archonId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(cascadeBluffsId, opposingArchonId);
    }

    @Test
    @DisplayName("Exiling another Archon does not trigger its dies ability")
    void exilingAnotherArchonDoesNotTriggerDeathAbility() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player2, new ArchonOfJustice());
        harness.addToBattlefield(player2, new CascadeBluffs());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new PunctureBlast(), new PunctureBlast()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID opposingArchonId = harness.getPermanentId(player2, "Archon of Justice");

        killArchonWithPunctureBlasts(archonId);

        harness.assertInGraveyard(player1, "Archon of Justice");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(archonId);
        harness.handlePermanentChosen(player1, opposingArchonId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Archon of Justice");
        harness.assertNotInGraveyard(player2, "Archon of Justice");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Archon of Justice"));
        harness.assertOnBattlefield(player2, "Cascade Bluffs");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void killArchonWithPunctureBlasts(UUID archonId) {
        harness.castAndResolveInstant(player2, 0, archonId);
        setupPlayer2Active();
        harness.castAndResolveInstant(player2, 0, archonId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
