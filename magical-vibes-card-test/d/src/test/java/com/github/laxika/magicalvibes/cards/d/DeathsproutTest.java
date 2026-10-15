package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArborealGrazer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KarnsBastion;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Deathsprout.class, Forest.class, ArborealGrazer.class, Island.class,
        GideonBlackblade.class, KarnsBastion.class})
class DeathsproutTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and prompts the caster to search their own library")
    void destroysCreatureAndPromptsForBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());
        setupLibrary(player1);
        harness.setLibrary(player2, List.of(new Island()));

        castDeathsprout(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arboreal Grazer");
        harness.assertInGraveyard(player2, "Arboreal Grazer");

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Island");
    }

    @Test
    @DisplayName("Puts the chosen basic land onto the caster's battlefield tapped")
    void chosenBasicLandEntersTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new ArborealGrazer(), new KarnsBastion()));
        harness.setLibrary(player2, List.of(new Island()));

        castDeathsprout(target);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent land = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(land.getCard().getId()).isEqualTo(forest.getId());
        assertThat(land.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Island");
        harness.assertInGraveyard(player1, "Deathsprout");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Deathsprout()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy the caster's own creature and still search")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        harness.setLibrary(player1, List.of(new Forest()));

        castDeathsprout(target);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Arboreal Grazer");
        harness.assertNotOnBattlefield(player1, "Arboreal Grazer");
        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still searches when the legal target is indestructible")
    void searchesWhenTargetIsIndestructible() {
        gd.activePlayerId = player1.getId();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        target.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        castDeathsprout(target);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gideon Blackblade");
        harness.assertNotInGraveyard(player1, "Gideon Blackblade");
        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not search when the target leaves before resolution")
    void doesNotSearchWhenTargetIsGone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(new Island()));

        castDeathsprout(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Deathsprout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can fail to find a basic land even when one is available")
    void canFailToFind() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        castDeathsprout(target);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Arboreal Grazer");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Deathsprout");
    }

    @Test
    @DisplayName("Still destroys the creature when the caster's library is empty")
    void destroysWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        harness.setLibrary(player1, List.of());

        castDeathsprout(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arboreal Grazer");
        harness.assertNotOnBattlefield(player1, "Arboreal Grazer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Deathsprout");
        assertThat(gd.stack).isEmpty();
    }

    private void castDeathsprout(Permanent target) {
        harness.setHand(player1, List.of(new Deathsprout()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Forest(), new Island(), new ArborealGrazer(), new KarnsBastion()));
    }
}
