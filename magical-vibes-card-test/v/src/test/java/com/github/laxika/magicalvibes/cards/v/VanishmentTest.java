package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vanishment.class, GrizzlyBears.class, Pacifism.class, Forest.class})
class VanishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target creature on top of its owner's library")
    void resolvingTucksCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Vanishment()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player2, "Vanishment");
    }

    @Test
    @DisplayName("Can target a nonland noncreature permanent such as an enchantment")
    void resolvingTucksEnchantment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new Pacifism());
        UUID pacifismId = harness.getPermanentId(player2, "Pacifism");
        Permanent pacifism = gqs.findPermanentById(gd, pacifismId);
        pacifism.setAttachedTo(bearsId);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Vanishment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, pacifismId);

        harness.assertNotOnBattlefield(player2, "Pacifism");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Forest());
        UUID landId = harness.getPermanentId(player1, "Forest");

        harness.setHand(player2, List.of(new Vanishment()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Miracle: drawing it as the first card lets it be cast for {U}")
    void miracleCastForOneBlue() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        Vanishment vanishment = new Vanishment();
        harness.setLibrary(player1, List.of(vanishment));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal

        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Vanishment()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player2, "Vanishment");
    }

    @Test
    @DisplayName("A stolen permanent goes to its owner's library")
    void stolenPermanentReturnsToOwnerLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, bears);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.stolenCreatures.put(targetId, player1.getId());
        int controllerDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Vanishment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSize);
    }

    @Test
    @DisplayName("Declining to reveal leaves Vanishment in hand without spending mana")
    void canDeclineMiracleReveal() {
        Vanishment vanishment = new Vanishment();
        harness.setLibrary(player1, List.of(vanishment));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Vanishment");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing does not force the player to cast Vanishment")
    void canDeclineMiracleCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Vanishment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Vanishment");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing Vanishment second in a turn does not offer miracle")
    void secondDrawDoesNotOfferMiracle() {
        harness.setLibrary(player1, List.of(new Forest(), new Vanishment()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.assertInHand(player1, "Vanishment");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
