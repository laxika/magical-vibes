package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FangrenMarauderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fangren Marauder has the any-artifact-to-graveyard triggered ability as a MayEffect")
    void hasCorrectEffects() {
        FangrenMarauder card = new FangrenMarauder();

        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(5);
        assertThat(may.prompt()).isEqualTo("Gain 5 life?");
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an opponent's artifact creature is destroyed")
    void triggersWhenOpponentArtifactCreatureDies() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent's non-creature artifact is destroyed")
    void triggersWhenOpponentNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
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
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // Fangren Marauder's may ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // No trigger — not an artifact
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Accepting the may ability gains 5 life")
    void acceptingMayAbilityGains5Life() {
        harness.addToBattlefield(player1, new FangrenMarauder());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

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

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

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

    // ===== Multiple triggers =====

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
        harness.castInstant(player1, 0, memniteId);
        harness.passBothPriorities(); // Resolve Naturalize

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);

        // Destroy second artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        // May ability goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Should have gained 5 life twice = 10 total
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 10);
    }
}
