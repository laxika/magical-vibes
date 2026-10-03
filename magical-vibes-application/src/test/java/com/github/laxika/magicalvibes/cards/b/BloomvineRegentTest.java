package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.d.DescendantOfStorms;
import com.github.laxika.magicalvibes.cards.d.DirgurIslandDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloomvineRegent.class, DirgurIslandDragon.class, Forest.class, DescendantOfStorms.class, Plains.class})
class BloomvineRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters")
    void gainsLifeWhenItEnters() {
        BloomvineRegent card = new BloomvineRegent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Gains 3 life when another Dragon you control enters")
    void gainsLifeWhenAnotherDragonEnters() {
        harness.addToBattlefield(player1, new BloomvineRegent());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Does not trigger for a non-Dragon creature")
    void doesNotTriggerForNonDragon() {
        harness.addToBattlefield(player1, new BloomvineRegent());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new DescendantOfStorms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Omen searches for basic Forests, putting one tapped onto the battlefield and one into hand")
    void omenSearchesForBasicForests() {
        Card card = new BloomvineRegent();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(2);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).allMatch(forest -> forest.getSubtypes().contains(CardSubtype.FOREST));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FOREST)
                        && permanent.isTapped());
        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(forest -> forest.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(gameData.playerDecks.get(player1.getId())).contains(card);
    }

    @Test
    @CardUsed({Conspiracy.class})
    @DisplayName("Still gains life for its own entry when its creature type is replaced")
    void gainsLifeForOwnEntryWithReplacedCreatureType() {
        harness.addToBattlefieldAndReturn(player1, new Conspiracy()).setChosenSubtype(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new BloomvineRegent()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Dragon")
    void doesNotTriggerForOpponentsDragon() {
        harness.addToBattlefield(player2, new BloomvineRegent());
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Omen with no matching Forest still shuffles itself into the library")
    void omenWithNoMatchingForests() {
        BloomvineRegent card = new BloomvineRegent();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(card, plains);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Omen finding only one Forest puts it onto the battlefield tapped")
    void omenFindsOnlyOneForest() {
        BloomvineRegent card = new BloomvineRegent();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Omen may find zero Forests even when matching cards exist")
    void omenMayFindZeroForests() {
        BloomvineRegent card = new BloomvineRegent();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(card, forest);
    }
}
