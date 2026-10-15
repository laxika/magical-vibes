package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElfhameWurm;
import com.github.laxika.magicalvibes.cards.w.WoodedRidgeline;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoutTheWilderness.class, Forest.class, ElfhameWurm.class, WoodedRidgeline.class})
class ScoutTheWildernessTest extends BaseCardTest {

    @Test
    void searchesForABasicLandAndPutsItOntoTheBattlefieldTapped() {
        Forest forest = new Forest();
        Card otherCard = new ElfhameWurm();
        harness.setLibrary(player1, List.of(forest, otherCard));
        harness.castFromHand(player1, new ScoutTheWilderness(), "{2}{G}");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    void kickedCastAlsoCreatesTwoSoldierTokens() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of(forest));
        addKickedMana();

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void kickedSpellCreatesSoldiersWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of());
        addKickedMana();

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertSoldiers();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedSpellCreatesSoldiersWhenOnlyANonbasicForestIsAvailable() {
        WoodedRidgeline nonbasic = new WoodedRidgeline();
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of(nonbasic));
        addKickedMana();

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wooded Ridgeline")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertSoldiers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedSpellStillCreatesSoldiersAfterChoosingNotToFindABasicLand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of(forest));
        addKickedMana();

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertSoldiers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void assertSoldiers() {
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.isTapped()).isFalse();
        });
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
