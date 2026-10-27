package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.AmuletOfQuoz;
import com.github.laxika.magicalvibes.cards.c.CrazedFirecat;
import com.github.laxika.magicalvibes.cards.c.CrookedScales;
import com.github.laxika.magicalvibes.cards.d.DesperateGambit;
import com.github.laxika.magicalvibes.cards.d.DwarvenBerserker;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.FieryGambit;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GameOfChaos;
import com.github.laxika.magicalvibes.cards.g.GoblinAssassin;
import com.github.laxika.magicalvibes.cards.m.ManaClash;
import com.github.laxika.magicalvibes.cards.m.ManaCrypt;
import com.github.laxika.magicalvibes.cards.r.RiskyMove;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@CardUsed({KrarksThumb.class, FieryGambit.class, AlphaMyr.class, ManaCrypt.class, CrazedFirecat.class,
        ManaClash.class, GoblinAssassin.class, FugitiveWizard.class, GameOfChaos.class, RiskyMove.class,
        ElvishWarrior.class, AmuletOfQuoz.class, CrookedScales.class, FreshVolunteers.class, DesperateGambit.class,
        DwarvenBerserker.class, KarplusanMinotaur.class})
class KrarksThumbTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a logical coin flip with two flips and ignores one")
    void replacesCoinFlip() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).anyMatch(log -> log.contains(
                "coin flip for Fiery Gambit (flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A Thumb controlled by the opponent does not replace your coin flip")
    void onlyTheFlippersControllerBenefitsFromThumb() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).isNotEmpty()
                .noneMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A tapped Thumb still replaces coin flips")
    void tappedThumbStillApplies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent thumb = harness.addToBattlefieldAndReturn(player1, new KrarksThumb());
        thumb.tap();
        castFieryGambit(target);

        assertThat(coinFlipLogs()).anyMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A Thumb that has lost all abilities does not replace coin flips")
    void thumbWithoutAbilitiesDoesNotApply() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent thumb = harness.addToBattlefieldAndReturn(player1, new KrarksThumb());
        thumb.setLosesAllAbilitiesUntilEndOfTurn(true);
        castFieryGambit(target);

        assertThat(coinFlipLogs()).isNotEmpty()
                .noneMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @ParameterizedTest
    @CsvSource({"Heads, 20", "Tails, 17"})
    @DisplayName("When the two flips disagree, the controller chooses which result to keep")
    void controllerChoosesWhichResultToKeep(String kept, int expectedLife) {
        harness.addToBattlefield(player1, new ManaCrypt());
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setLife(player1, 20);
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(false, true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.assertLife(player1, 20);
            harness.handleListChoice(player1, kept);
        }

        harness.assertLife(player1, expectedLife);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Mana Crypt (flipped 2 coins and ignored 1)")))
                .hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"Heads, true", "Tails, false"})
    @DisplayName("Fiery Gambit's first flip waits for the kept result before anything happens")
    void fieryGambitFirstFlipAsksForKeptResult(String kept, boolean offeredToFlipAgain) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        try (var coins = mockFlips(true, false)) {
            harness.castAndResolveSorcery(player1, 0, target.getId());

            assertThat(coinFlipLogs()).isEmpty();
            harness.handleListChoice(player1, kept);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null)
                .isEqualTo(offeredToFlipAgain);
        assertThat(coinFlipLogs()).singleElement().asString()
                .contains(kept.equals("Heads") ? "wins the coin flip" : "loses the coin flip");
    }

    @Test
    @DisplayName("A later flip of a flip-until-lose loop asks for its kept result and keeps the earlier wins")
    void flipUntilLoseLaterFlipAsksForKeptResult() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.castFromHand(player1, new CrazedFirecat(), "{5}{R}{R}");
        harness.passBothPriorities();
        Permanent firecat = findPermanent(player1, "Crazed Firecat");

        // Win (both heads), then a split flip kept as heads, then a loss (both tails).
        try (var coins = mockFlips(true, true, true, false, false, false)) {
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(firecatFlipLogs()).singleElement().asString().contains("wins the coin flip");
            assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            harness.handleListChoice(player1, "Heads");
        }

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firecatFlipLogs()).hasSize(3);
        assertThat(firecatFlipLogs().getLast()).contains("loses the coin flip");
    }

    @Test
    @DisplayName("Choosing tails on a later flip of a flip-until-lose loop ends the loop there")
    void flipUntilLoseLaterFlipKeptAsTails() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.castFromHand(player1, new CrazedFirecat(), "{5}{R}{R}");
        harness.passBothPriorities();
        Permanent firecat = findPermanent(player1, "Crazed Firecat");

        try (var coins = mockFlips(true, true, false, true)) {
            harness.passBothPriorities();
            harness.handleListChoice(player1, "Tails");
        }

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firecatFlipLogs()).hasSize(2);
        assertThat(firecatFlipLogs().getLast()).contains("loses the coin flip");
    }

    @Test
    @DisplayName("Mana Clash asks for a later round's kept result without dealing an earlier round's damage again")
    void manaClashLaterRoundAsksForKeptResult() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ManaClash()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Round 1: controller heads (both coins), opponent tails. Round 2: controller split, opponent heads.
        try (var coins = mockFlips(true, true, false, false, true, true)) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.assertLife(player2, 19);
            harness.handleListChoice(player1, "Heads");
        }

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(manaClashRounds()).hasSize(2);
    }

    @Test
    @DisplayName("The opponent chooses the kept result of its own Thumb-replaced Mana Clash flip")
    void manaClashOpponentChoosesItsKeptResult() {
        harness.addToBattlefield(player2, new KrarksThumb());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ManaClash()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Round 1: controller tails, opponent split (kept as tails). Round 2: both heads.
        try (var coins = mockFlips(false, true, false, true, true, true)) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.assertLife(player1, 20);
            harness.handleListChoice(player2, "Tails");
        }

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(manaClashRounds()).hasSize(2);
    }

    @Test
    @DisplayName("Each player's flip of Goblin Assassin is flipped before its Thumb-replaced result is chosen")
    void eachPlayerFlipAsksForKeptResult() {
        harness.addToBattlefield(player2, new KrarksThumb());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new GoblinAssassin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Player 1 flips heads; player 2's two coins disagree and it keeps tails.
        try (var coins = mockFlips(true, true, false)) {
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.assertOnBattlefield(player2, "Fugitive Wizard");
            harness.handleListChoice(player2, "Tails");
        }

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Goblin Assassin");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Goblin Assassin"))).hasSize(2);
    }

    @ParameterizedTest
    @CsvSource({"Heads, true", "Tails, false"})
    @DisplayName("Fiery Gambit's flip after \"flip again\" waits for the kept result before it is logged")
    void fieryGambitFlipAgainAsksForKeptResult(String kept, boolean offeredToFlipAgain) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        try (var coins = mockFlips(true, true, true, false)) {
            harness.castAndResolveSorcery(player1, 0, target.getId());
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(coinFlipLogs()).hasSize(1);
            harness.handleListChoice(player1, kept);
        }

        assertThat(coinFlipLogs()).hasSize(2);
        assertThat(coinFlipLogs().getLast())
                .contains(kept.equals("Heads") ? "wins the coin flip" : "loses the coin flip");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null)
                .isEqualTo(offeredToFlipAgain);
    }

    @Test
    @DisplayName("Game of Chaos's first flip waits for the kept result before any life changes")
    void gameOfChaosFirstFlipAsksForKeptResult() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);
        try (var coins = mockFlips(true, false)) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.handleListChoice(player1, "Tails");
        }

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        assertThat(gameOfChaosFlipLogs()).singleElement().asString().contains("loses the flip (stakes 1)");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Game of Chaos's \"flip again\" waits for the kept result and swaps the doubled stake once")
    void gameOfChaosFlipAgainAsksForKeptResult() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);
        // First flip: both heads (controller wins). Second flip: split, kept as tails.
        try (var coins = mockFlips(true, true, false, true)) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.assertLife(player1, 21);
            harness.assertLife(player2, 19);
            assertThat(gameOfChaosFlipLogs()).hasSize(1);
            harness.handleListChoice(player1, "Tails");
        }

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        assertThat(gameOfChaosFlipLogs()).hasSize(2);
        assertThat(gameOfChaosFlipLogs().getLast()).contains("loses the flip (stakes 2)");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @ParameterizedTest
    @CsvSource({"Heads, true", "Tails, false"})
    @DisplayName("Risky Move's flip after its creature choice waits for the kept result")
    void riskyMoveFlipAfterCreatureChoiceAsksForKeptResult(String kept, boolean keepsCreature) {
        harness.addToBattlefield(player1, new RiskyMove());
        harness.addToBattlefield(player2, new KrarksThumb());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        advanceToUpkeep(player2);
        resolveAllTriggers();

        try (var coins = mockFlips(true, false)) {
            harness.handlePermanentChosen(player2, chosen.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player2.getId());
            assertThat(gameLogContains("coin flip for Risky Move")).isFalse();
            harness.handleListChoice(player2, kept);
        }

        assertThat(gd.playerBattlefields.get(player2.getId()).contains(chosen)).isEqualTo(keepsCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()).contains(chosen)).isEqualTo(!keepsCreature);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Risky Move"))).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"Heads, true", "Tails, false"})
    @DisplayName("Amulet of Quoz's flip after a declined ante waits for the kept result")
    void amuletOfQuozDeclinedAnteAsksForKeptResult(String kept, boolean controllerWins) {
        harness.addToBattlefield(player1, new AmuletOfQuoz());
        harness.addToBattlefield(player1, new KrarksThumb());
        advanceToUpkeep(player1);
        harness.setLibrary(player2, List.of(new AlphaMyr()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        try (var coins = mockFlips(true, false)) {
            harness.handleMayAbilityChosen(player2, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
            harness.handleListChoice(player1, kept);
        }

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(controllerWins ? player1.getId() : player2.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("doesn't ante"))).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Amulet of Quoz"))).hasSize(1);
    }

    @Test
    @DisplayName("Amulet of Quoz's immediate flip against an empty library waits for the kept result")
    void amuletOfQuozEmptyLibraryAsksForKeptResult() {
        harness.addToBattlefield(player1, new AmuletOfQuoz());
        harness.addToBattlefield(player1, new KrarksThumb());
        advanceToUpkeep(player1);
        harness.setLibrary(player2, List.of());
        harness.activateAbility(player1, 0, null, player2.getId());

        try (var coins = mockFlips(true, false)) {
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
            harness.handleListChoice(player1, "Tails");
        }

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Crooked Scales's repeated flip after paying {3} waits for the kept result")
    void crookedScalesRepeatAsksForKeptResult() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        harness.addToBattlefield(player1, new KrarksThumb());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));

        // First flip: both tails (lost). Repeated flip: split, kept as heads.
        try (var coins = mockFlips(false, false, true, false)) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.handleListChoice(player1, "Heads");
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crooked Scales"))).hasSize(2);
    }

    @ParameterizedTest
    @CsvSource({"Heads, 2", "Tails, 0"})
    @DisplayName("Desperate Gambit's flip after its source choice waits for the kept result")
    void desperateGambitFlipAfterSourceChoiceAsksForKeptResult(String kept, int damageMultiplier) {
        Permanent source = addCreatureReady(player1, new DwarvenBerserker());
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setHand(player1, List.of(new DesperateGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        try (var coins = mockFlips(true, false)) {
            harness.handlePermanentChosen(player1, source.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
            harness.handleListChoice(player1, kept);
        }

        assertThat(gd.sourceNextDamageToAnyTargetShields).singleElement()
                .satisfies(shield -> assertThat(shield.damageMultiplier()).isEqualTo(damageMultiplier));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Desperate Gambit"))).hasSize(1);
    }

    @Test
    @DisplayName("Each flip of a flip-a-coin cumulative upkeep waits for its kept result before any trigger")
    void cumulativeUpkeepFlipsAskForKeptResults() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new KarplusanMinotaur());
        minotaur.setCounterCount(CounterType.AGE, 1);
        harness.addToBattlefield(player1, new KrarksThumb());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        try (var coins = mockFlips(true, false, false, true)) {
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(minotaurFlipLogs()).isEmpty();
            harness.handleListChoice(player1, "Heads");

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            assertThat(minotaurFlipLogs()).isEmpty();
            harness.handleListChoice(player1, "Tails");
        }

        assertThat(minotaurFlipLogs()).hasSize(2);
        assertThat(minotaurFlipLogs().getFirst()).contains("wins the coin flip");
        assertThat(minotaurFlipLogs().getLast()).contains("loses the coin flip");
        harness.assertOnBattlefield(player1, "Karplusan Minotaur");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    private List<String> minotaurFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Karplusan Minotaur"))
                .toList();
    }

    private List<String> gameOfChaosFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.startsWith("Game of Chaos:"))
                .toList();
    }

    private MockedStatic<ThreadLocalRandom> mockFlips(Boolean first, Boolean... rest) {
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(first, rest);
        MockedStatic<ThreadLocalRandom> coins = mockStatic(ThreadLocalRandom.class);
        coins.when(ThreadLocalRandom::current).thenReturn(random);
        return coins;
    }

    private List<String> firecatFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crazed Firecat"))
                .toList();
    }

    private List<String> manaClashRounds() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.startsWith("Mana Clash:"))
                .toList();
    }

    private void castFieryGambit(Permanent target) {
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (true) {
            PendingInteraction.ColorChoice resultChoice =
                    gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            if (resultChoice != null) {
                harness.handleListChoice(player1, "Tails");
            } else if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                return;
            }
        }
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fiery Gambit"))
                .toList();
    }
}
