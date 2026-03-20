package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandToHandRepeatIfHighMVEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemonlordBelzenlokTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Demonlord Belzenlok has ETB exile-until-nonland effect")
    void hasEtbEffect() {
        DemonlordBelzenlok card = new DemonlordBelzenlok();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(ExileUntilNonlandToHandRepeatIfHighMVEffect.class);

        ExileUntilNonlandToHandRepeatIfHighMVEffect effect =
                (ExileUntilNonlandToHandRepeatIfHighMVEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.manaValueThreshold()).isEqualTo(4);
        assertThat(effect.damagePerCard()).isEqualTo(1);
    }

    // ===== ETB trigger behavior =====

    @Test
    @DisplayName("Stops on first nonland card with mana value less than 4")
    void stopsOnLowMVNonland() {
        // Library: Forest, GrizzlyBears (MV 2)
        // Expected: Forest exiled, GrizzlyBears to hand, 1 damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (puts ETB on stack)
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Repeats when nonland card has mana value 4 or greater")
    void repeatsOnHighMVNonland() {
        // Library: SerraAngel (MV 5), GrizzlyBears (MV 2)
        // Expected: SerraAngel to hand (MV >= 4, repeat), GrizzlyBears to hand (MV < 4, stop), 2 damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new SerraAngel(), new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiles lands and repeats across multiple iterations")
    void exilesLandsAndRepeatsMultipleIterations() {
        // Library: Forest, SerraAngel (MV 5), Forest, Forest, SuntailHawk (MV 1)
        // Iteration 1: exile Forest (land), SerraAngel to hand (MV >= 4, repeat)
        // Iteration 2: exile Forest (land), exile Forest (land), SuntailHawk to hand (MV < 4, stop)
        // Expected: 3 Forests exiled, 2 cards to hand, 2 damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new SerraAngel(), new Forest(), new Forest(), new SuntailHawk()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("First card is nonland with low MV — no lands exiled")
    void firstCardIsLowMVNonland() {
        // Library: GrizzlyBears (MV 2)
        // Expected: GrizzlyBears to hand, 1 damage, no lands exiled
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Library is all lands — exiles everything, no damage")
    void allLandsInLibrary() {
        // Library: Forest, Forest, Forest
        // Expected: all 3 exiled, no cards to hand, no damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Empty library — no effect, no damage")
    void emptyLibrary() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Library runs out during repeat — stops gracefully")
    void libraryRunsOutDuringRepeat() {
        // Library: SerraAngel (MV 5)
        // Expected: SerraAngel to hand (MV >= 4, repeat), library empty, stop. 1 damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new SerraAngel()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Multiple high-MV cards chain together")
    void multipleHighMVCardsChain() {
        // Library: SerraAngel (MV 5), SerraAngel (MV 5), GrizzlyBears (MV 2)
        // Expected: 2 SerraAngels + 1 GrizzlyBears to hand = 3 cards, 3 damage
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new SerraAngel(), new SerraAngel(), new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        long serraCount = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Serra Angel")).count();
        assertThat(serraCount).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Damage log mentions correct card count")
    void damageLogMentionsCardCount() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new SerraAngel(), new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Demonlord Belzenlok") && log.contains("damage") && log.contains("2 cards put into hand"));
    }

    @Test
    @DisplayName("Lands exiled before nonland remain in exile")
    void landsRemainInExile() {
        // Library: Forest, Forest, GrizzlyBears (MV 2)
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new Forest(), new GrizzlyBears()
        )));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonlordBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExiledCards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
