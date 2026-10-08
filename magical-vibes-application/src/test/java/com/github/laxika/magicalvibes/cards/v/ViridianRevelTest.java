package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViridianRevel.class, Memnite.class, MindStone.class, Naturalize.class,
        CruelEdict.class, GrizzlyBears.class})
class ViridianRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when an opponent's artifact creature is destroyed")
    void triggersWhenOpponentArtifactCreatureDies() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Memnite");

        // Viridian Revel's may ability should prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent's non-creature artifact is destroyed")
    void triggersWhenOpponentNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Mind Stone");

        // Viridian Revel's may ability should prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does NOT trigger when own artifact is destroyed")
    void doesNotTriggerForOwnArtifact() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player1, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        // Player2 destroys player1's Mind Stone
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, mindStoneId);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Mind Stone");

        // No trigger — own artifact, not opponent's
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No trigger — not an artifact
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingMayAbilityDrawsCard() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Hand is now empty after casting Cruel Edict
        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Declining the may ability does not draw a card")
    void decliningMayAbilityDoesNotDrawCard() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // No card drawn (hand size = before - 1 for casting Cruel Edict)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Triggers separately for each opponent artifact destroyed")
    void triggersForEachArtifactSeparately() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new MindStone());

        UUID memniteId = harness.getPermanentId(player2, "Memnite");
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        // Destroy first artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, memniteId);
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Destroy second artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // The key assertion: both triggers fired and both drew cards
        harness.assertInGraveyard(player2, "Memnite");
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Triggers for an opponent-owned artifact even when you control it")
    void triggersForOpponentOwnedArtifactYouControl() {
        harness.addToBattlefield(player1, new ViridianRevel());
        Memnite artifact = new Memnite();
        artifact.setOwnerId(player2.getId());
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, artifact).getId();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Memnite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, artifactId);

        harness.assertInGraveyard(player2, "Memnite");
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Memnite");
    }

    @Test
    @DisplayName("Does not trigger for your artifact controlled by an opponent")
    void doesNotTriggerForOwnArtifactControlledByOpponent() {
        harness.addToBattlefield(player1, new ViridianRevel());
        Memnite artifact = new Memnite();
        artifact.setOwnerId(player1.getId());
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, artifactId);

        harness.assertInGraveyard(player1, "Memnite");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
