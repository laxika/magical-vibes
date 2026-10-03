package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.cards.f.FavorableWinds;
import com.github.laxika.magicalvibes.cards.o.OtherworldAtlas;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishingStroke.class, WanderingWolf.class, Mountain.class, FavorableWinds.class, OtherworldAtlas.class})
class BanishingStrokeTest extends BaseCardTest {

    private void giveFullCost() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Resolving puts the target creature on the bottom of its owner's library")
    void resolvingTucksCreature() {
        harness.addToBattlefield(player2, new WanderingWolf());
        UUID targetId = harness.getPermanentId(player2, "Wandering Wolf");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BanishingStroke()));
        giveFullCost();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Wandering Wolf");
        harness.assertNotInGraveyard(player2, "Wandering Wolf");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getLast().getName()).isEqualTo("Wandering Wolf");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new WanderingWolf());
        harness.addToBattlefield(player2, new Mountain());
        UUID landId = harness.getPermanentId(player2, "Mountain");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BanishingStroke()));
        giveFullCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or enchantment");
    }

    @Test
    @DisplayName("Miracle cast for {W} off the first draw tucks the chosen creature")
    void miracleCastTucksCreature() {
        harness.addToBattlefield(player2, new WanderingWolf());
        UUID targetId = harness.getPermanentId(player2, "Wandering Wolf");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setLibrary(player1, List.of(new BanishingStroke()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal

        harness.passBothPriorities(); // resolve miracle trigger and open cast prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wandering Wolf");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getLast().getName()).isEqualTo("Wandering Wolf");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the miracle reveal leaves the card in hand")
    void decliningRevealLeavesInHand() {
        BanishingStroke stroke = new BanishingStroke();
        harness.setLibrary(player1, List.of(stroke));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(stroke.getId()));
    }

    @Test
    @DisplayName("Resolving tucks a noncreature artifact")
    void resolvingTucksArtifact() {
        assertTucksPermanent(new OtherworldAtlas());
    }

    @Test
    @DisplayName("Resolving tucks a noncreature enchantment")
    void resolvingTucksEnchantment() {
        assertTucksPermanent(new FavorableWinds());
    }

    private void assertTucksPermanent(Card target) {
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, target.getName());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new BanishingStroke()));
        giveFullCost();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, target.getName());
        harness.assertNotInGraveyard(player2, target.getName());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("The second draw of a turn does not offer miracle")
    void secondDrawDoesNotOfferMiracle() {
        BanishingStroke stroke = new BanishingStroke();
        harness.setLibrary(player1, List.of(new Mountain(), stroke));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(stroke);
    }

    @Test
    @DisplayName("Declining to cast after revealing keeps the card in hand and spends no mana")
    void decliningMiracleCastKeepsCardAndMana() {
        BanishingStroke stroke = new BanishingStroke();
        harness.addToBattlefield(player2, new WanderingWolf());
        harness.setLibrary(player1, List.of(stroke));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(stroke);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Wandering Wolf");
    }

    @Test
    @DisplayName("Miracle is available for the first draw during an opponent's turn")
    void miracleCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WanderingWolf());
        UUID targetId = harness.getPermanentId(player2, "Wandering Wolf");
        BanishingStroke stroke = new BanishingStroke();
        harness.setLibrary(player1, List.of(stroke));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wandering Wolf");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Wandering Wolf");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(stroke);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
