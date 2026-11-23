package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SeizeTheSecrets;
import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.k.KarplusanMinotaur;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesTheBlastmaker.class, SeizeTheSecrets.class, BoomBox.class, EdgarKingOfFigaro.class,
        KarplusanMinotaur.class})
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
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new BoomBox());

        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiPermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactlyInAnyOrder(artifact.getId(), otherArtifact.getId());

        int lifeBefore = gd.getLife(player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));
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
    @DisplayName("Declining the sacrifice does not flip, and the third spell does not trigger")
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
    @DisplayName("Without an artifact there is no coin flip")
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
    @DisplayName("Spells cast before Breeches enters count toward the second spell")
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
    @DisplayName("The flip happens on resolution, but the winning copy waits for a separate trigger")
    void sacrificeFlipsImmediatelyButWinningCopyWaitsForSeparateTrigger() {
        addBreechesAndArtifact();
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gameLogContains("wins the coin flip for Breeches, the Blastmaker")).isTrue();
        assertThat(gameLogContains("A copy of Seize the Secrets is created.")).isFalse();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gameLogContains("A copy of Seize the Secrets is created.")).isTrue();
        resolveAllTriggers();
    }

    @Test
    @CardUsed({EdgarKingOfFigaro.class})
    @DisplayName("Winning the flip copies a permanent spell as a token")
    void winningFlipCopiesPermanentSpellAsToken() {
        addBreechesAndArtifact();
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new BoomBox(), "{2}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Boom Box"))
                .hasSize(2)
                .anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue())
                .anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }
    @Test
    @DisplayName("Breeches's flip is won or lost: either result triggers Karplusan Minotaur")
    void eitherFlipResultTriggersCoinFlipAbilities() {
        addBreechesAndArtifact();
        harness.addToBattlefield(player1, new KarplusanMinotaur());
        castSeizeTheSecretsAndResolve();
        harness.castFromHand(player1, new SeizeTheSecrets(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean lostFlip = gameLogContains("loses the coin flip for Breeches, the Blastmaker");
        assertThat(lostFlip || gameLogContains("wins the coin flip for Breeches, the Blastmaker")).isTrue();
        for (int step = 0; step < 10 && (gd.interaction.isAwaitingInput() || !gd.stack.isEmpty()); step++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice choice) {
                var chooser = choice.playerId().equals(player1.getId()) ? player1 : player2;
                var other = chooser == player1 ? player2 : player1;
                harness.handlePermanentChosen(chooser, other.getId());
            } else {
                resolveAllTriggers();
            }
        }

        int breechesDamage = lostFlip ? 3 : 0;
        int totalDamage = (20 - gd.getLife(player1.getId())) + (20 - gd.getLife(player2.getId()));
        assertThat(totalDamage - breechesDamage).as("Karplusan Minotaur's 1 damage").isEqualTo(1);
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
