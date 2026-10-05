package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
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

@CardUsed({JetsBrainwashing.class, OtterPenguin.class, Forest.class})
class JetsBrainwashingTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the target cannot block and a Clue is created")
    void withoutKickerCantBlockAndCreatesClue() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("With kicker, steals, untaps, and gives haste to the target")
    void withKickerStealsUntapsAndGivesHaste() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        target.tap();
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Kicker's temporary control and grants expire at cleanup")
    void kickerEffectsExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Creating the Clue does not investigate or trigger Illuminator")
    @CardUsed(ErdwalIlluminator.class)
    void creatingClueDoesNotInvestigate() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("An illegal sole target prevents even the Clue from being created")
    void removedTargetPreventsClueCreation() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Jet's Brainwashing");
        harness.assertInHand(player2, "Otter-Penguin");
    }

    @Test
    @DisplayName("Kicker can untap and grant haste to a creature already controlled by the caster")
    void kickedSpellCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        target.tap();
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Without kicker a tapped target stays tapped and the Clue can be sacrificed to draw")
    void unkickedSpellLeavesTargetTappedAndCreatesUsableClue() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());
        target.tap();
        harness.setHand(player1, List.of(new JetsBrainwashing()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        Permanent clue = findPermanents(player1, "Clue").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }
}
