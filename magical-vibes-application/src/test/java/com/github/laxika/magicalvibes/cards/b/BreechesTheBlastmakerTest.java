package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SeizeTheSecrets;
import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesTheBlastmaker.class, SeizeTheSecrets.class, BoomBox.class, EdgarKingOfFigaro.class})
class BreechesTheBlastmakerTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell does not trigger Breeches")
    void firstSpellDoesNotTrigger() {
        Permanent artifact = addBreechesAndArtifact();

        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The second spell offers an artifact sacrifice and resolves one coin-flip branch")
    void secondSpellOffersArtifactSacrifice() {
        Permanent artifact = addBreechesAndArtifact();

        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
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

        harness.assertInGraveyard(player1, "Boom Box");
        boolean copied = gameLogContains("A copy of Seize the Secrets is created.");
        boolean lostFlip = gameLogContains("loses the coin flip for Breeches, the Blastmaker");
        assertThat(copied || lostFlip).isTrue();
        if (lostFlip) {
            assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        }
    }

    @Test
    void decliningSacrificeDoesNotFlipAndThirdSpellDoesNotTrigger() {
        Permanent artifact = addBreechesAndArtifact();
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gameLogContains("the coin flip for Breeches, the Blastmaker")).isFalse();

        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    void noArtifactMeansNoCoinFlip() {
        harness.addToBattlefield(player1, new BreechesTheBlastmaker());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("the coin flip for Breeches, the Blastmaker")).isFalse();
    }

    @Test
    void spellsCastBeforeBreechesEntersCountTowardSecondSpell() {
        castSeizeTheSecretsAndResolve();
        Permanent artifact = addBreechesAndArtifact();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @CardUsed({EdgarKingOfFigaro.class})
    void sacrificeFlipsImmediatelyButWinningCopyWaitsForSeparateTrigger() {
        Permanent artifact = addBreechesAndArtifact();
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gameLogContains("wins the coin flip for Breeches, the Blastmaker")).isTrue();
        assertThat(gameLogContains("A copy of Seize the Secrets is created.")).isFalse();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gameLogContains("A copy of Seize the Secrets is created.")).isTrue();
        resolveAllTriggers();
    }

    @Test
    @CardUsed({EdgarKingOfFigaro.class})
    void winningFlipCopiesPermanentSpellAsToken() {
        Permanent artifact = addBreechesAndArtifact();
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new BoomBox(), "{2}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Boom Box"))
                .hasSize(2)
                .anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue())
                .anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }
    private Permanent addBreechesAndArtifact() {
        harness.addToBattlefield(player1, new BreechesTheBlastmaker());
        return harness.addToBattlefieldAndReturn(player1, new BoomBox());
    }

    private void castSeizeTheSecretsAndResolve() {
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();
    }
}
