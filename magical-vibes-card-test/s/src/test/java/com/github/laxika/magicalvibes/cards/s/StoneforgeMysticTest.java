package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({StoneforgeMystic.class, LeoninScimitar.class, GrizzlyBears.class})
class StoneforgeMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and offers an Equipment search that puts the chosen card into hand")
    void etbSearchesForEquipment() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Leonin Scimitar");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the enter-the-battlefield search leaves the library unchanged")
    void decliningEtbSearchDoesNothing() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Leonin Scimitar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability puts only an Equipment card from hand onto the battlefield")
    void activatedAbilityPutsEquipmentFromHandOntoBattlefield() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        harness.setHand(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(mystic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Equipment search may fail to find even when Equipment is present")
    void mayFailToFindEquipment() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Leonin Scimitar", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library with no Equipment completes without a choice")
    void searchWithNoEquipment() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining to put Equipment onto the battlefield leaves it in hand")
    void decliningActivatedAbilityKeepsEquipmentInHand() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating without Equipment cannot put another card onto the battlefield")
    void activatedAbilityWithNoEquipment() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires both the generic and white mana costs")
    void cannotActivateWithOnlyOneWhiteMana() {
        addCreatureReady(player1, new StoneforgeMystic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Mystic cannot pay the tap cost")
    void cannotActivateWhenTapped() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        mystic.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Mystic cannot activate its tap ability")
    void cannotActivateWithSummoningSickness() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        mystic.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mystic.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment is chosen from the current hand when the ability resolves")
    void choosesEquipmentAtResolution() {
        addCreatureReady(player1, new StoneforgeMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LeoninScimitar()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertNotInHand(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
