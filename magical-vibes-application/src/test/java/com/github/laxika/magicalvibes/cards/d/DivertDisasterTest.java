package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivertDisaster.class, GrizzlyBears.class, Forest.class})
class DivertDisasterTest extends BaseCardTest {

    @Test
    void countersSpellWhenItsControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DivertDisaster()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void payingTwoLetsSpellResolveAndCreatesLander() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new DivertDisaster()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).hasSize(1);
    }
    @Test
    void decliningAffordablePaymentCountersSpellWithoutCreatingLander() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new DivertDisaster()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void paidRiderLanderCanImmediatelySacrificeToFindTappedBasicLand() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        DivertDisaster nonland = new DivertDisaster();
        harness.setLibrary(player2, List.of(nonland, forest));
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new DivertDisaster()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player2, "Lander");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(lander), null, null);
        assertThat(lander.isTapped()).isTrue();
        assertThat(findPermanents(player2, "Lander")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland);
    }
}

