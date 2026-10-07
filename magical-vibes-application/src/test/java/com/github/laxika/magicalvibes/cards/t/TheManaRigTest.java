package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.f.FusionElemental;
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

@CardUsed({TheManaRig.class, FusionElemental.class, ArcaneSignet.class})
class TheManaRigTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell creates a tapped Powerstone")
    void multicoloredSpellCreatesTappedPowerstone() {
        harness.addToBattlefield(player1, new TheManaRig());
        harness.setHand(player1, List.of(new FusionElemental()));
        addFusionElementalMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability puts up to two of the top X cards into hand")
    void activatedAbilityChoosesUpToTwoCards() {
        Card first = new ArcaneSignet();
        Card second = new FusionElemental();
        Card third = new ArcaneSignet();
        Card untouched = new FusionElemental();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.addToBattlefield(player1, new TheManaRig());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third);
        assertThat(findPermanent(player1, "The Mana Rig").isTapped()).isTrue();
    }

    @Test
    void colorlessSpellDoesNotCreatePowerstone() {
        harness.addToBattlefield(player1, new TheManaRig());
        harness.setHand(player1, List.of(new ArcaneSignet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Powerstone");
    }

    @Test
    void opponentsMulticoloredSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new TheManaRig());
        harness.setHand(player1, List.of(new FusionElemental()));
        addFusionElementalMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Powerstone");
        harness.assertNotOnBattlefield(player2, "Powerstone");
    }

    @Test
    void mayChooseNoCardsFromThreeLookedAtCards() {
        Card first = new ArcaneSignet();
        Card second = new FusionElemental();
        Card third = new ArcaneSignet();
        Card untouched = new FusionElemental();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new TheManaRig());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, third, untouched);
    }

    @Test
    void mayDeclineTheOnlyLookedAtCard() {
        Card first = new ArcaneSignet();
        Card untouched = new FusionElemental();
        harness.setLibrary(player1, List.of(first, untouched));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new TheManaRig());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, first);
    }

    @Test
    void zeroXDoesNotMoveAnyCards() {
        Card first = new ArcaneSignet();
        harness.setLibrary(player1, List.of(first));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new TheManaRig());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(findPermanent(player1, "The Mana Rig").isTapped()).isTrue();
    }

    private void addFusionElementalMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}

