package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gigantiform.class, StoneworkPuma.class, SpidersilkNet.class})
class GigantiformTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has base power and toughness 8/8 and trample")
    void enchantsCreatureWithGiantStatsAndTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        castGigantiform(creature.getId(), false);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kicked Gigantiform offers a named battlefield search")
    void kickedEtbSearchesForGigantiform() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setLibrary(player1, List.of(new StoneworkPuma(), new Gigantiform(), new SpidersilkNet()));

        castGigantiform(creature.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).singleElement()
                .extracting(Card::getName).isEqualTo("Gigantiform");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An un-kicked Gigantiform does not search the library")
    void unKickedEtbDoesNotSearch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setLibrary(player1, List.of(new Gigantiform()));

        castGigantiform(creature.getId(), false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gigantiform cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        harness.setHand(player1, List.of(new Gigantiform()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The searched Aura can enchant a different creature and is not kicked")
    void searchedAuraChoosesAttachmentWithoutAnotherSearch() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Gigantiform found = new Gigantiform();
        Gigantiform remaining = new Gigantiform();
        harness.setLibrary(player1, List.of(found, remaining));

        castGigantiform(first.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(found.getId()))
                .singleElement().extracting(Permanent::getAttachedTo).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may decline a kicked Gigantiform's search")
    void kickedSearchCanBeDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Gigantiform libraryCard = new Gigantiform();
        harness.setLibrary(player1, List.of(libraryCard));
        castGigantiform(creature.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The restricted search may fail to find a Gigantiform")
    void kickedSearchMayFailToFind() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Gigantiform libraryCard = new Gigantiform();
        harness.setLibrary(player1, List.of(libraryCard));
        castGigantiform(creature.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gigantiform changes base stats while preserving equipment bonuses and abilities")
    void baseStatsDoNotOverwriteEquipmentBonuses() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.addToBattlefield(player1, new SpidersilkNet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        castGigantiform(creature.getId(), false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    private void castGigantiform(java.util.UUID targetId, boolean kicked) {
        harness.setHand(player1, List.of(new Gigantiform()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 7 : 3);
        if (kicked) {
            harness.getGameService().playCard(gd, player1, 0, 0, targetId, null,
                    List.of(), List.of(), false, null, null, null, null, null, true);
        } else {
            harness.castEnchantment(player1, 0, targetId);
        }
    }
}
