package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.a.ArrestersAdmonition;
import com.github.laxika.magicalvibes.cards.q.Quasiduplicate;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@CardUsed({MirrorMarch.class, AxebaneBeast.class, ArrestersAdmonition.class, Quasiduplicate.class})
class MirrorMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one hasty token copy for each won flip and exiles them at the next end step")
    void createsHastyTokenCopyForEachWonFlipAndExilesThemAtEndStep() {
        harness.addToBattlefield(player1, new MirrorMarch());
        harness.setHand(player1, List.of(new AxebaneBeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        long wonFlips = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("wins the coin flip for Mirror March"))
                .count();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize((int) wonFlips);
        assertThat(tokens).allSatisfy(token -> assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .filteredOn(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP)
                .hasSize((int) wonFlips);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Losing the first flip creates no tokens")
    void losingFirstFlipCreatesNoTokens() {
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(false);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castBeastWithMirrorMarch();
            resolveAllTriggers();
            assertThat(countPermanents(player1, "Axebane Beast")).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            assertThat(gameLogContains("loses the coin flip for Mirror March")).isTrue();
        }
    }

    @Test
    @DisplayName("Copies the entering creature using last known information after it is bounced")
    void copiesCreatureAfterItLeavesBattlefield() {
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true, false);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castBeastWithMirrorMarch();
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(1);
            Permanent beast = findPermanent(player1, "Axebane Beast");
            harness.setHand(player2, List.of(new ArrestersAdmonition()));
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.castAndResolveInstant(player2, 0, beast.getId());
            harness.assertInHand(player1, "Axebane Beast");
            resolveAllTriggers();
            assertThat(findPermanents(player1, "Axebane Beast")).singleElement()
                    .satisfies(token -> {
                        assertThat(token.getCard().isToken()).isTrue();
                        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
                    });
        }
    }

    @Test
    @DisplayName("A copy of a Mirror March token does not inherit its granted haste")
    void grantedHasteIsNotCopiable() {
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true, false);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castBeastWithMirrorMarch();
            resolveAllTriggers();
            Permanent firstToken = findPermanents(player1, "Axebane Beast").stream()
                    .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
            harness.setHand(player1, List.of(new Quasiduplicate()));
            harness.addMana(player1, ManaColor.BLUE, 3);
            harness.castSorcery(player1, 0, firstToken.getId());
            resolveAllTriggers();
            assertThat(findPermanents(player1, "Axebane Beast")).hasSize(3);
            Permanent secondToken = findPermanents(player1, "Axebane Beast").stream()
                    .filter(p -> p.getCard().isToken() && !p.getId().equals(firstToken.getId()))
                    .findFirst().orElseThrow();
            assertThat(gqs.hasKeyword(gd, firstToken, Keyword.HASTE)).isTrue();
            assertThat(gqs.hasKeyword(gd, secondToken, Keyword.HASTE)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Multiple tokens are exiled by one delayed ability controlled by Mirror March's controller")
    void exileUsesOneDelayedTrigger() {
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true, true, false);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castBeastWithMirrorMarch();
            resolveAllTriggers();
            assertThat(findPermanents(player1, "Axebane Beast")).hasSize(3);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.passUntil(TurnStep.END_STEP);
            assertThat(gd.stack).singleElement().satisfies(trigger -> {
                assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
                assertThat(trigger.getCard()).isInstanceOf(MirrorMarch.class);
            });
            assertThat(findPermanents(player1, "Axebane Beast")).hasSize(3);
            resolveAllTriggers();
            assertThat(findPermanents(player1, "Axebane Beast")).singleElement()
                    .satisfies(beast -> assertThat(beast.getCard().isToken()).isFalse());
        }
    }

    private void castBeastWithMirrorMarch() {
        harness.addToBattlefield(player1, new MirrorMarch());
        harness.setHand(player1, List.of(new AxebaneBeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
    }
}
