package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CantankerousKeepers.class, AmoeboidChangeling.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, LeylineOfTheVoid.class})
class CantankerousKeepersTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Elves reduces only the controller's generic cost")
    void affinityForElvesReducesGenericCost() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new CantankerousKeepers()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("ETB mills four and returns every milled Elf card to hand")
    void millsFourAndReturnsAllMilledElves() {
        LlanowarElves elf1 = new LlanowarElves();
        LlanowarElves elf2 = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        LlanowarElves fifthElf = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf1, bears, elf2, forest, fifthElf));
        castKeepers();

        assertThat(gd.playerHands.get(player1.getId())).contains(elf1, elf2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifthElf);
    }

    @Test
    @DisplayName("A milled changeling card counts as an Elf card")
    void changelingCountsAsElf() {
        AmoeboidChangeling changeling = new AmoeboidChangeling();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(changeling, bears));
        castKeepers();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    private void castKeepers() {
        harness.castFromHand(player1, new CantankerousKeepers(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Affinity counts changelings and cannot reduce the green mana requirement")
    void affinityWithMoreThanFiveElvesStillCostsGreen() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new AmoeboidChangeling());
        }

        harness.castFromHand(player1, new CantankerousKeepers(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only newly milled Elves are returned, not Elves already in the graveyard")
    void noMilledElvesLeavesExistingGraveyardElfAlone() {
        LlanowarElves existingElf = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        gd.playerGraveyards.get(player1.getId()).add(existingElf);
        harness.setLibrary(player1, List.of(bears, forest));

        castKeepers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(existingElf, bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milled Elves diverted to exile are still put into hand")
    void returnsMilledElvesFromExile() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        LlanowarElves elf = new LlanowarElves();
        AmoeboidChangeling changeling = new AmoeboidChangeling();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(elf, changeling, bears, forest));

        castKeepers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(elf, changeling);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(bears, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
