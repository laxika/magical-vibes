package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScalpelexisTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        harness.getGameData().playerDecks.put(player.getId(), new ArrayList<>(cards));
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Scalpelexis has correct card properties")
    void hasCorrectProperties() {
        Scalpelexis card = new Scalpelexis();

        assertThat(card.getName()).isEqualTo("Scalpelexis");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(5);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(ExileTopCardsRepeatOnDuplicateEffect.class);
        assertThat(((ExileTopCardsRepeatOnDuplicateEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst()).count())
                .isEqualTo(4);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage exiles top four cards when all names are unique")
    void exilesTopFourCardsNoDuplicates() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new GrizzlyBears() // fifth card should not be exiled
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Repeats when two exiled cards share the same name")
    void repeatsOnDuplicateNames() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        // First batch: two GrizzlyBears share a name -> repeat
        // Second batch: all unique -> stop
        setDeck(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new Scalpelexis(),
                new SteadfastGuard(),
                new SkyhunterProwler()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("repeating the process"));
    }

    @Test
    @DisplayName("Multiple consecutive repeats when duplicates keep appearing")
    void multipleRepeats() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        // First batch: two GrizzlyBears -> repeat
        // Second batch: two SerraAngels -> repeat
        // Third batch: all unique -> stop
        setDeck(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SerraAngel(),
                new SerraAngel(),
                new SamiteHealer(),
                new Scalpelexis(),
                new SuntailHawk(),
                new SteadfastGuard(),
                new SkyhunterProwler(),
                new GrizzlyBears()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(12);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when Scalpelexis is blocked")
    void noTriggerWhenBlocked() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Handles library with fewer than four cards")
    void partialLibraryExile() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles empty library gracefully")
    void emptyLibrary() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Stops repeating when library runs out mid-repeat")
    void libraryRunsOutDuringRepeat() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        // First batch: duplicate names -> repeat
        // Second batch: only 2 cards left in library
        setDeck(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new Scalpelexis()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Scalpelexis")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Exiled cards are the correct cards from top of library in order")
    void exiledCardsAreCorrectFromTopOfLibrary() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new Scalpelexis()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        List<Card> exiledCards = gd.playerExiledCards.get(player2.getId());
        assertThat(exiledCards).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Serra Angel", "Suntail Hawk", "Samite Healer");
    }

    @Test
    @DisplayName("Game log records exiled card names")
    void gameLogRecordsExiledCards() {
        Permanent scalpelexis = addReadyCreature(player1, new Scalpelexis());
        scalpelexis.setAttacking(true);

        setDeck(player2, List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("Serra Angel"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles cards from the top"));
    }
}

