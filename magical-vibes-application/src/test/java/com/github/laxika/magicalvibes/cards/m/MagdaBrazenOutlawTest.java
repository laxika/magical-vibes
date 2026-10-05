package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.g.GoldspanDragon;
import com.github.laxika.magicalvibes.cards.v.VaultRobber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagdaBrazenOutlaw.class, VaultRobber.class, FearlessPup.class, GoldveinPick.class, GoldspanDragon.class})
class MagdaBrazenOutlawTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dwarves you control get +1/+0")
    void boostsOtherDwarvesYouControl() {
        addCreatureReady(player1, new MagdaBrazenOutlaw());
        Permanent dwarf = addCreatureReady(player1, creature("Dwarf", CardSubtype.DWARF));
        Permanent nonDwarf = addCreatureReady(player1, creature("Bear", CardSubtype.BEAR));
        Permanent opponentDwarf = addCreatureReady(player2, creature("Opponent Dwarf", CardSubtype.DWARF));

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonDwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonDwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentDwarf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Whenever a Dwarf you control becomes tapped, create a Treasure")
    void createsTreasureWhenDwarfBecomesTapped() {
        addCreatureReady(player1, new MagdaBrazenOutlaw());
        Permanent dwarf = addCreatureReady(player1, creature("Dwarf", CardSubtype.DWARF));

        tapAndResolve(dwarf);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .singleElement()
                .satisfies(treasure -> {
                    assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
                    assertThat(treasure.getCard().isToken()).isTrue();
                });
    }

    @Test
    @DisplayName("Sacrificing five Treasures searches for an artifact or Dragon")
    void sacrificesFiveTreasuresAndSearchesForArtifactOrDragon() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        for (int i = 0; i < 5; i++) {
            addTreasureToken(player1);
        }

        Card artifact = artifact("Artifact Card");
        Card dragon = creature("Dragon Card", CardSubtype.DRAGON);
        Card nonMatching = creature("Bear Card", CardSubtype.BEAR);
        harness.setLibrary(player1, List.of(artifact, dragon, nonMatching));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Artifact Card", "Dragon Card");

        int dragonIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Dragon Card");
        harness.handleCardChosen(player1, dragonIndex);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == dragon);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE));
    }

    @Test
    @DisplayName("Cannot activate without five Treasures")
    void cannotActivateWithoutFiveTreasures() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        for (int i = 0; i < 4; i++) {
            addTreasureToken(player1);
        }

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Magda does not boost herself, but boosts another Dwarf")
    void excludesHerselfFromBoost() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        Permanent dwarf = addCreatureReady(player1, new VaultRobber());

        assertThat(gqs.getEffectivePower(gd, magda)).isEqualTo(magda.getCard().getPower());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(dwarf.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(dwarf.getCard().getToughness());
    }

    @Test
    @DisplayName("Magda creates a Treasure when she herself becomes tapped")
    void createsTreasureWhenMagdaBecomesTapped() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());

        tapAndResolve(magda);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Tapping a non-Dwarf or an opponent's Dwarf creates no Treasure")
    void ignoresNonDwarvesAndOpponentDwarves() {
        addCreatureReady(player1, new MagdaBrazenOutlaw());
        Permanent nonDwarf = addCreatureReady(player1, new FearlessPup());
        Permanent opponentDwarf = addCreatureReady(player2, new VaultRobber());

        nonDwarf.tap();
        opponentDwarf.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, nonDwarf);
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, opponentDwarf);
        });

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Magda can search for an artifact")
    void activatesWhileTappedAndSummoningSick() {
        Permanent magda = harness.addToBattlefieldAndReturn(player1, new MagdaBrazenOutlaw());
        magda.setSummoningSick(true);
        magda.tap();
        for (int i = 0; i < 5; i++) {
            addTreasureToken(player1);
        }
        Card artifact = new GoldveinPick();
        Card nonMatching = new FearlessPup();
        harness.setLibrary(player1, List.of(artifact, nonMatching));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(artifact);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Goldvein Pick").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatching);
        assertThat(magda.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Magda may fail to find even when an eligible artifact is in the library")
    void mayFailToFind() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        for (int i = 0; i < 5; i++) {
            addTreasureToken(player1);
        }
        Card artifact = new GoldveinPick();
        harness.setLibrary(player1, List.of(artifact));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        harness.assertNotOnBattlefield(player1, "Goldvein Pick");
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Magda puts a nonartifact Dragon onto the battlefield without paying its mana cost")
    void searchesForNonartifactDragon() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        for (int i = 0; i < 5; i++) {
            addTreasureToken(player1);
        }
        Card dragon = new GoldspanDragon();
        Card dwarf = new VaultRobber();
        harness.setLibrary(player1, List.of(dragon, dwarf));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(dragon);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Goldspan Dragon").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dwarf);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Opponent's Treasures cannot pay Magda's sacrifice cost")
    void cannotSacrificeOpponentTreasures() {
        Permanent magda = addCreatureReady(player1, new MagdaBrazenOutlaw());
        for (int i = 0; i < 4; i++) {
            addTreasureToken(player1);
        }
        addTreasureToken(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(magda), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void addTreasureToken(Player player) {
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setSubtypes(List.of(CardSubtype.TREASURE));
        treasure.setToken(true);

        Permanent permanent = new Permanent(treasure);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
