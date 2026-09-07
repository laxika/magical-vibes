package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesTheBlastmaker.class, Divination.class, LeoninScimitar.class})
class BreechesTheBlastmakerTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell does not trigger Breeches")
    void firstSpellDoesNotTrigger() {
        Permanent artifact = addBreechesAndArtifact();

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The second spell offers an artifact sacrifice and resolves one coin-flip branch")
    void secondSpellOffersArtifactSacrifice() {
        Permanent artifact = addBreechesAndArtifact();

        castDivinationAndResolve();
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactly(artifact.getId());

        int lifeBefore = gd.getLife(player2.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        if (gd.interaction.activeInteraction() == null) {
            resolveAllTriggers();
        }

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        boolean copied = gameLogContains("A copy of Divination is created.");
        boolean lostFlip = gameLogContains("loses the coin flip for Breeches, the Blastmaker");
        assertThat(copied || lostFlip).isTrue();
        if (lostFlip) {
            assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        }
    }

    private Permanent addBreechesAndArtifact() {
        harness.addToBattlefield(player1, new BreechesTheBlastmaker());
        return harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
    }

    private void castDivinationAndResolve() {
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
    }
}
