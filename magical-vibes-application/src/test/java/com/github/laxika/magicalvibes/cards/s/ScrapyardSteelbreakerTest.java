package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.p.PatchworkAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapyardSteelbreaker.class, PatchworkAutomaton.class, JukaiTrainee.class})
class ScrapyardSteelbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact gives Scrapyard Steelbreaker +2/+1 until end of turn")
    void sacrificesArtifactAndBoostsSelf() {
        Permanent steelbreaker = addCreatureReady(player1, new ScrapyardSteelbreaker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(artifact.getId(), otherArtifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(steelbreaker.getPowerModifier()).isEqualTo(2);
        assertThat(steelbreaker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent steelbreaker = addCreatureReady(player1, new ScrapyardSteelbreaker());
        harness.addToBattlefield(player1, new PatchworkAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(steelbreaker.getPowerModifier()).isZero();
        assertThat(steelbreaker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without another artifact to sacrifice")
    void requiresAnotherArtifact() {
        Permanent steelbreaker = addCreatureReady(player1, new ScrapyardSteelbreaker());
        harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and repeated activations accumulate")
    void sacrificesAsCostAndStacksBoostsWhileSummoningSickAndTapped() {
        Permanent steelbreaker = harness.addToBattlefieldAndReturn(player1, new ScrapyardSteelbreaker());
        steelbreaker.setSummoningSick(true);
        steelbreaker.tap();
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());
        harness.addToBattlefield(player2, new PatchworkAutomaton());
        harness.addToBattlefield(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());
        harness.handlePermanentChosen(player1, firstArtifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstArtifact.getCard());
        assertThat(steelbreaker.getPowerModifier()).isZero();
        assertThat(steelbreaker.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstArtifact.getCard(), secondArtifact.getCard());
        assertThat(steelbreaker.getPowerModifier()).isEqualTo(4);
        assertThat(steelbreaker.getToughnessModifier()).isEqualTo(2);
        assertThat(steelbreaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent steelbreaker = addCreatureReady(player1, new ScrapyardSteelbreaker());
        harness.addToBattlefield(player2, new PatchworkAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana cost must be paid as well as sacrificing an artifact")
    void requiresMana() {
        Permanent steelbreaker = addCreatureReady(player1, new ScrapyardSteelbreaker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(steelbreaker), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

