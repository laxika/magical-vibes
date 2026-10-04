package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HammerOfNazahn;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NazahnReveredBladesmith.class, HammerOfNazahn.class, LeoninScimitar.class, GrizzlyBears.class})
class NazahnReveredBladesmithTest extends BaseCardTest {

    @Test
    @DisplayName("Puts Hammer of Nazahn onto the battlefield and other Equipment into hand")
    void searchesForEquipmentWithSpecialHammerDestination() {
        harness.setHand(player1, List.of(new NazahnReveredBladesmith(), new HammerOfNazahn()));
        harness.setLibrary(player1, List.of(new HammerOfNazahn(), new LeoninScimitar()));
        addNazahnMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Hammer of Nazahn", "Leonin Scimitar");

        int hammerIndex = search.params().cards().indexOf(search.params().cards().stream()
                .filter(card -> card.getName().equals("Hammer of Nazahn"))
                .findFirst()
                .orElseThrow());
        harness.handleCardChosen(player1, hammerIndex);

        harness.assertOnBattlefield(player1, "Hammer of Nazahn");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hammer of Nazahn");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Leonin Scimitar");
    }

    @Test
    @DisplayName("Puts a searched Equipment other than Hammer of Nazahn into hand")
    void searchesOtherEquipmentIntoHand() {
        harness.setHand(player1, List.of(new NazahnReveredBladesmith()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        addNazahnMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Nazahn, Revered Bladesmith");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Taps a defending creature when an equipped creature attacks and accepts the may")
    void attackTriggerMayTapDefendingCreature() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent nazahn = addReady(player1, new NazahnReveredBladesmith());
        nazahn.setAttachedTo(attacker.getId());
        Permanent defender = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(defender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap when the attack trigger's may is declined")
    void attackTriggerMayBeDeclined() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent nazahn = addReady(player1, new NazahnReveredBladesmith());
        nazahn.setAttachedTo(attacker.getId());
        Permanent defender = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defender.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addNazahnMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
