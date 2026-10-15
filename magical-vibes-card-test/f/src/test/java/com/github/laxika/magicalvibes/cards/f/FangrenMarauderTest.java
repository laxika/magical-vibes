package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangrenMarauder.class, CruelEdict.class, GrizzlyBears.class, Memnite.class,
        MindStone.class, Naturalize.class, WrathOfGod.class})
class FangrenMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when an opponent's artifact creature is destroyed")
    void triggersWhenOpponentArtifactCreatureDies() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Memnite");

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent's non-creature artifact is destroyed")
    void triggersWhenOpponentNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Mind Stone");

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when own artifact is destroyed (unlike Viridian Revel)")
    void triggersForOwnArtifact() {
        harness.addToBattlefield(player1, new FangrenMarauder());
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

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new FangrenMarauder());
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
    @DisplayName("Accepting the may ability gains 5 life")
    void acceptingMayAbilityGains5Life() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Player1 should have gained 5 life
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Declining the may ability does not gain life")
    void decliningMayAbilityDoesNotGainLife() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Fangren Marauder"));

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Triggers separately for each artifact destroyed")
    void triggersForEachArtifactSeparately() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new MindStone());

        UUID memniteId = harness.getPermanentId(player2, "Memnite");
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Destroy first artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, memniteId);

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);

        // Destroy second artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Should have gained 5 life twice = 10 total
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 10);
    }

    @Test
    @DisplayName("Artifact sacrificed as an activation cost triggers before the activated ability resolves")
    void triggersForArtifactSacrificedAsCost() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 5);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sees every artifact creature dying simultaneously with Marauder")
    void triggersWhenMarauderAndArtifactsDieTogether() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fangren Marauder");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 10);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
