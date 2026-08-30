package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(artifact, dragon, nonMatching));

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
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(dragonIndex));

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
