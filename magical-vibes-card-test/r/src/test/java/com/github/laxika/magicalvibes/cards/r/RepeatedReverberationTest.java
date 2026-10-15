package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WingedWords;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RepeatedReverberation.class, ChandraNovicePyromancer.class, AirElemental.class,
        WingedWords.class, Shock.class})
class RepeatedReverberationTest extends BaseCardTest {

    @Test
    @DisplayName("copies the next instant or sorcery spell twice")
    void copiesNextSpellTwice() {
        castRepeatedReverberation();
        harness.setLibrary(player1, List.of(
                new AirElemental(), new AirElemental(), new AirElemental(),
                new AirElemental(), new AirElemental(), new AirElemental()));
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("copies the next loyalty ability twice")
    void copiesNextLoyaltyAbilityTwice() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1,
                new AirElemental());
        castRepeatedReverberation();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount.get(player1.getId())).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).doesNotContainKey(player1.getId());

        resolveAllTriggers();

        assertThat(elemental.getPowerModifier()).isEqualTo(6);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void castingASpellConsumesTheLoyaltyCopyAsWell() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castRepeatedReverberation();
        castAndResolveWingedWords();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void activatingLoyaltyConsumesTheSpellCopyAsWell() {
        addReadyChandra(player1, 5);
        castRepeatedReverberation();
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        castAndResolveWingedWords();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    void oneDelayedTriggerCreatesBothSpellCopiesBeforeEitherResolves() {
        castRepeatedReverberation();
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void eachInstantCopyMayChooseItsOwnTargetAndLaterInstantsAreNotCopied() {
        castRepeatedReverberation();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
    }

    @Test
    void targetedLoyaltyCopiesMayChangeTargetsWithoutPayingLoyaltyAgain() {
        Permanent chandra = addReadyChandra(player1, 5);
        castRepeatedReverberation();
        harness.activateAbility(player1, 0, 2, null, player2.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void opponentSpellsDoNotConsumeTheDelayedTrigger() {
        castRepeatedReverberation();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);

        castAndResolveWingedWords();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    void castingACreatureDoesNotConsumeTheDelayedTrigger() {
        castRepeatedReverberation();
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        castAndResolveWingedWords();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    void successiveRepeatedReverberationsCopyTheFollowingSpellSixTimes() {
        castRepeatedReverberation();
        harness.setHand(player1, List.of(new RepeatedReverberation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        castAndResolveWingedWords();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(14);
    }

    @Test
    void unusedDelayedTriggerExpiresAtTheEndOfTheTurn() {
        castRepeatedReverberation();
        harness.setLibrary(player2, List.of(new AirElemental(), new AirElemental()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveWingedWords() {
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, 14)
                .mapToObj(i -> new AirElemental()).toList());
        harness.setHand(player1, List.of(new WingedWords()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();
    }

    private void castRepeatedReverberation() {
        harness.setHand(player1, List.of(new RepeatedReverberation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(2);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
