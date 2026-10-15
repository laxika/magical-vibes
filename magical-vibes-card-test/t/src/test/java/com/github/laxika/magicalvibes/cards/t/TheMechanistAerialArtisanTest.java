package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TheMechanistAerialArtisan.class, GrizzlyBears.class, MindStone.class, ErdwalIlluminator.class})
class TheMechanistAerialArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue when you cast a noncreature spell")
    void createsClueForNoncreatureSpell() {
        addMechanistReady();

        castMindStone();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Clue when you cast a creature spell")
    void doesNotCreateClueForCreatureSpell() {
        addMechanistReady();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Animates a target artifact token you control")
    void animatesTargetArtifactToken() {
        Permanent clue = addMechanistAndClue();

        harness.activateAbility(player1, 0, null, clue.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(clue)).isTrue();
        assertThat(gqs.isCreature(gd, clue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, clue)).isEqualTo(1);
        assertThat(clue.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(gqs.hasKeyword(gd, clue, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-token artifact")
    void cannotTargetNonTokenArtifact() {
        addMechanistReady();
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact token you control");
    }

    @Test
    @DisplayName("Animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent clue = addMechanistAndClue();

        harness.activateAbility(player1, 0, null, clue.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, clue)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(clue.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, clue)).isFalse();
        assertThat(clue.getTransientSubtypes()).isEmpty();
        assertThat(gqs.hasKeyword(gd, clue, Keyword.FLYING)).isFalse();
    }

    private Permanent addMechanistAndClue() {
        addMechanistReady();
        castMindStone();
        return findPermanent(player1, "Clue");
    }

    private void addMechanistReady() {
        addCreatureReady(player1, new TheMechanistAerialArtisan());
    }

    private void castMindStone() {
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }

    @Test
    void createsClueBeforeNoncreatureSpellResolves() {
        addMechanistReady();
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Mind Stone")).isEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
    }

    @Test
    void creatingClueDoesNotInvestigate() {
        addMechanistReady();
        harness.addToBattlefield(player1, new ErdwalIlluminator());

        castMindStone();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotCreateClue() {
        addMechanistReady();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void cannotTargetOpponentsArtifactToken() {
        addMechanistReady();
        addCreatureReady(player2, new TheMechanistAerialArtisan());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        Permanent clue = findPermanent(player2, "Clue");
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, clue.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact token you control");
    }

    @Test
    void animatedClueRetainsSacrificeAbilityWithoutTapCost() {
        Permanent clue = addMechanistAndClue();
        harness.activateAbility(player1, 0, null, clue.getId());
        resolveAllTriggers();
        clue.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void animationPreservesCountersAndDoesNotAnimateOtherTokens() {
        Permanent clue = addMechanistAndClue();
        castMindStone();
        Permanent otherClue = findPermanents(player1, "Clue").stream()
                .filter(permanent -> !permanent.getId().equals(clue.getId()))
                .findFirst().orElseThrow();
        clue.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, clue.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, clue)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, otherClue)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherClue, Keyword.FLYING)).isFalse();
    }
}
