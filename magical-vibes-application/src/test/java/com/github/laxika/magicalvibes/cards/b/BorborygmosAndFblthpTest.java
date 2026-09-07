package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorborygmosAndFblthp.class, Forest.class, GrizzlyBears.class, Island.class})
class BorborygmosAndFblthpTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws, discards lands, and deals twice that much damage")
    void entersDrawsAndDamagesForDiscardedLands() {
        harness.setHand(player1, List.of(new BorborygmosAndFblthp(), new Forest(), new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBorborygmosMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent borborygmos = findPermanent(player1, "Borborygmos and Fblthp");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, borborygmos.getId());
        harness.passBothPriorities();

        assertThat(borborygmos.getMarkedDamage()).isEqualTo(4);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Attacking draws and uses the number of discarded lands for the reflexive damage")
    void attackTriggersDrawAndDiscardDamage() {
        Permanent borborygmos = addCreatureReady(player1, new BorborygmosAndFblthp());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(borborygmos)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, borborygmos.getId());
        harness.passBothPriorities();

        assertThat(borborygmos.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability puts the source third from the top of its owner's library")
    void putsSelfThirdFromTop() {
        harness.addToBattlefield(player1, new BorborygmosAndFblthp());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(2).getName()).isEqualTo("Borborygmos and Fblthp");
    }

    private void addBorborygmosMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
