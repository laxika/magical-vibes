package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurieEmergentIntelligence.class, JhoirasFamiliar.class, GrizzlyBears.class,
        EnsoulArtifact.class, SolemnSimulacrum.class, CodsworthHandyHelper.class})
class CurieEmergentIntelligenceTest extends BaseCardTest {

    @Test
    @DisplayName("Curie becomes a permanent copy of the artifact creature exiled to pay its ability")
    void copiesExiledArtifactCreaturePermanently() {
        Permanent curie = addCreatureReady(player1, new CurieEmergentIntelligence());
        Permanent familiar = addCreatureReady(player1, new JhoirasFamiliar());
        activateCopyAbility();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
        assertThat(curie.getCard().getName()).isEqualTo("Jhoira's Familiar");
        assertThat(curie.getCard().getPower()).isEqualTo(2);
        assertThat(curie.getCard().getToughness()).isEqualTo(2);
        assertThat(curie.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(curie.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Curie's copied combat trigger draws the copied creature's base power")
    void copiedCombatTriggerDrawsBasePower() {
        Permanent curie = addCreatureReady(player1, new CurieEmergentIntelligence());
        addCreatureReady(player1, new JhoirasFamiliar());
        activateCopyAbility();
        curie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        assertThat(gqs.getEffectivePower(gd, curie)).isEqualTo(3);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Curie's original combat trigger ignores counters")
    void originalCombatTriggerDrawsBasePower() {
        Permanent curie = addCreatureReady(player1, new CurieEmergentIntelligence());
        curie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new CurieEmergentIntelligence(), new CurieEmergentIntelligence()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 1);
    }

    @Test
    @DisplayName("Curie draws using base power set by an Aura, excluding counters")
    void drawsPowerSetByEnsoulArtifact() {
        Permanent curie = addCreatureReady(player1, new CurieEmergentIntelligence());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, curie.getId());
        harness.passBothPriorities();
        curie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new CurieEmergentIntelligence(), new CurieEmergentIntelligence(),
                new CurieEmergentIntelligence(), new CurieEmergentIntelligence(),
                new CurieEmergentIntelligence(), new CurieEmergentIntelligence()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        assertThat(gqs.getEffectivePower(gd, curie)).isEqualTo(6);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Queued copy activations retain their own exile choices")
    void queuedActivationsKeepSeparateExileChoices() {
        Permanent curie = addCreatureReady(player1, new CurieEmergentIntelligence());
        Permanent solemn = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent codsworth = addCreatureReady(player1, new CodsworthHandyHelper());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, solemn.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(solemn);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(codsworth);
        resolveAllTriggers();

        assertThat(curie.getCard().getName()).isEqualTo("Solemn Simulacrum");
    }

    @Test
    @DisplayName("Curie cannot exile itself or an opposing artifact creature")
    void requiresAnotherArtifactCreatureYouControl() {
        addCreatureReady(player1, new CurieEmergentIntelligence());
        addCreatureReady(player2, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Curie cannot exile an artifact creature token")
    void cannotExileToken() {
        addCreatureReady(player1, new CurieEmergentIntelligence());
        SolemnSimulacrum token = new SolemnSimulacrum();
        token.setToken(true);
        addCreatureReady(player1, token);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Curie cannot exile a nonartifact creature")
    void cannotExileNonartifactCreature() {
        addCreatureReady(player1, new CurieEmergentIntelligence());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A pending combat trigger uses Curie's new base power after copying")
    void pendingCombatTriggerUsesNewCopyPower() {
        addCreatureReady(player1, new CurieEmergentIntelligence());
        addCreatureReady(player1, new SolemnSimulacrum());
        harness.setLibrary(player1, List.of(new CurieEmergentIntelligence(), new CurieEmergentIntelligence(),
                new CurieEmergentIntelligence()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void activateCopyAbility() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
