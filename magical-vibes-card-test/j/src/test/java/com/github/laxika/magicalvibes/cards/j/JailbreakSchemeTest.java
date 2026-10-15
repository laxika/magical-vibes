package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JailbreakScheme.class, AngelicChorus.class, DarksteelCitadel.class, GrizzlyBears.class, Island.class})
class JailbreakSchemeTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode adds a counter and makes the creature unblockable until end of turn")
    void counterModeAddsCounterAndUnblockable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 4);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Library mode can target an artifact land and its owner chooses the bottom")
    void libraryModeTargetsArtifactLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(new int[]{1}, List.of(target.getId()), 3);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both modes resolve and charge their separate additional costs")
    void bothModesResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), 6);
        harness.handleListChoice(player2, "Top");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isCantBeBlocked()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(artifact.getCard(), topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Each mode enforces its own target restriction")
    void modesRejectWrongTargetTypes() {
        Permanent artifactLand = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        assertThatThrownBy(() -> cast(new int[]{0}, List.of(artifactLand.getId()), 4))
                .isInstanceOf(IllegalStateException.class);

        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        assertThatThrownBy(() -> cast(new int[]{1}, List.of(enchantment.getId()), 3))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new JailbreakScheme()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both modes can target the same creature and resolve in printed order")
    void bothModesCanShareCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()), 6);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isCantBeBlocked()).isTrue();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
    }

    @Test
    @DisplayName("A stolen creature's owner chooses its destination in the owner's library")
    void ownerChoosesForStolenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of());

        cast(new int[]{1}, List.of(target.getId()), 3);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Top"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The library mode still resolves when the counter mode's target leaves")
    void remainingLegalTargetStillResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new JailbreakScheme()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.isCantBeBlocked()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, artifact.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Both modes cannot be cast without paying both additional costs")
    void bothModesRequireFullAdditionalCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        assertThatThrownBy(() -> cast(new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()), 5))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(JailbreakScheme.class::isInstance);
    }

    @Test
    @DisplayName("The counter mode still resolves when the library mode's target leaves")
    void counterModeResolvesWhenLibraryTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new JailbreakScheme()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerHands.get(player2.getId()).add(artifact.getCard());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isCantBeBlocked()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(JailbreakScheme.class::isInstance);
    }

    @Test
    @DisplayName("The spell does not resolve when its shared target leaves the battlefield")
    void allTargetsIllegalPreventsResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new JailbreakScheme()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(target.getId(), target.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(JailbreakScheme.class::isInstance);
    }
}
