package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingSilcaw.class, Spellbook.class, GrizzlyBears.class, SerraAngel.class,
        SuntailHawk.class, SamiteHealer.class, Scalpelexis.class})
class ScreechingSilcawTest extends BaseCardTest {

    @Test
    @DisplayName("Losing metalcraft before resolution prevents milling")
    void losingMetalcraftBeforeResolutionPreventsMill() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new SerraAngel(),
                new SuntailHawk(), new SamiteHealer()));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Gaining metalcraft after damage cannot create a trigger")
    void gainingMetalcraftAfterDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new SerraAngel(),
                new SuntailHawk(), new SamiteHealer()));

        resolveCombat();
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Opponent's artifacts do not satisfy metalcraft")
    void opponentsArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new SerraAngel(),
                new SuntailHawk(), new SamiteHealer()));

        resolveCombat();
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
    }

    @Test
    @DisplayName("Dealing combat damage mills 4 cards when metalcraft is met")
    void millsFourCardsWithMetalcraft() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Samite Healer");
    }

    @Test
    @DisplayName("Milled cards go to graveyard in order from top of library")
    void milledCardsGoToGraveyard() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new Scalpelexis()
        ));

        resolveCombat();
        resolveAllTriggers();

        List<Card> graveyard = gd.playerGraveyards.get(player2.getId());
        assertThat(graveyard).extracting(Card::getName)
                .contains("Grizzly Bears", "Serra Angel", "Suntail Hawk", "Samite Healer");
    }

    @Test
    @DisplayName("Game log records metalcraft mill trigger")
    void gameLogRecordsMillTrigger() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gameLogContains("metalcraft ability triggers")).isTrue();
        assertThat(gameLogContains("mills 4 card")).isTrue();
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Silcaw")
    void defenderTakesCombatDamage() {
        setupMetalcraft();
        harness.setLife(player2, 20);
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No mill when metalcraft is not met (0 artifacts)")
    void noMillWithoutMetalcraft() {
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("No mill with only 2 artifacts")
    void noMillWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Defender still takes combat damage even without metalcraft")
    void defenderTakesDamageWithoutMetalcraft() {
        harness.setLife(player2, 20);
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when Silcaw is blocked")
    void noTriggerWhenBlocked() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(3); // Silcaw is at index 3 (after 3 Spellbooks)

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Handles library with fewer than 4 cards")
    void partialLibraryMill() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles empty library gracefully")
    void emptyLibrary() {
        setupMetalcraft();
        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mills with exactly 3 artifacts (metalcraft threshold)")
    void millsWithExactlyThreeArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent silcaw = addCreatureReady(player1, new ScreechingSilcaw());
        silcaw.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
