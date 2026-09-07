package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TrumpetingCarnosaur.class, ElspethKnightErrant.class, Forest.class,
        GrizzlyBears.class, HillGiant.class})
class TrumpetingCarnosaurTest extends BaseCardTest {

    @Test
    @DisplayName("When Trumpeting Carnosaur enters, it discovers 5")
    void discoversFiveWhenItEnters() {
        Forest land = new Forest();
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        addCarnosaurMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("The hand ability deals 3 damage to a creature and discards Trumpeting Carnosaur")
    void handAbilityDamagesCreature() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addHandAbilityMana();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateHandAbility(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Trumpeting Carnosaur");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The hand ability deals 3 damage to a planeswalker")
    void handAbilityDamagesPlaneswalker() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        addHandAbilityMana();

        harness.activateHandAbility(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Trumpeting Carnosaur");
    }

    private void addCarnosaurMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addHandAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
