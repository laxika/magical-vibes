package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RottingMastodon;
import com.github.laxika.magicalvibes.cards.k.KheruBloodsucker;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({DeadDrop.class, RottingMastodon.class, Forest.class, KheruBloodsucker.class})
class DeadDropTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and the target opponent chooses two creatures to sacrifice")
    void delvesAndTargetOpponentSacrificesTwoCreatures() {
        List<Card> graveyard = List.of(
                new RottingMastodon(), new RottingMastodon(), new RottingMastodon(),
                new RottingMastodon(), new RottingMastodon(), new RottingMastodon(),
                new RottingMastodon(), new RottingMastodon(), new RottingMastodon());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.addToBattlefield(player1, new RottingMastodon());
        harness.addToBattlefield(player2, new RottingMastodon());
        harness.addToBattlefield(player2, new RottingMastodon());
        harness.addToBattlefield(player2, new RottingMastodon());
        harness.addToBattlefield(player2, new Forest());

        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(),
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        List<Permanent> creatures = findPermanents(player2, "Rotting Mastodon");
        harness.handleMultiplePermanentsChosen(player2,
                List.of(creatures.get(0).getId(), creatures.get(1).getId()));

        assertThat(findPermanents(player2, "Rotting Mastodon")).hasSize(1);
        assertThat(findPermanents(player2, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Rotting Mastodon")).hasSize(1);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player1, new RottingMastodon());
        harness.addToBattlefield(player1, new RottingMastodon());
        harness.addToBattlefield(player2, new RottingMastodon());

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Rotting Mastodon");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof RottingMastodon).hasSize(2);
        harness.assertOnBattlefield(player2, "Rotting Mastodon");
    }

    @Test
    void sacrificesOnlyAvailableCreatureWithoutDelving() {
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player2, new RottingMastodon());
        harness.addToBattlefield(player2, new Forest());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Rotting Mastodon");
        harness.assertInGraveyard(player2, "Rotting Mastodon");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Dead Drop");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetPlayerWithoutCreaturesAndPayPartlyWithDelve() {
        Card exiled = new Forest();
        Card retained = new RottingMastodon();
        harness.setGraveyard(player1, List.of(exiled, retained));
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player2, new Forest());

        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(retained);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Dead Drop");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousSacrificePreservesDyingCreaturesDeathTrigger() {
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new KheruBloodsucker());
        harness.addToBattlefield(player2, new RottingMastodon());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Kheru Bloodsucker");
        harness.assertInGraveyard(player2, "Rotting Mastodon");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void chosenCreaturesAreSacrificedSimultaneously() {
        harness.setHand(player1, List.of(new DeadDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new KheruBloodsucker());
        harness.addToBattlefield(player2, new RottingMastodon());
        harness.addToBattlefield(player2, new RottingMastodon());

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(
                harness.getPermanentId(player2, "Kheru Bloodsucker"),
                findPermanents(player2, "Rotting Mastodon").getFirst().getId()));

        assertThat(findPermanents(player2, "Rotting Mastodon")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }
}
