package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BristlebudFarmer.class, Forest.class, GrizzlyBears.class, Opt.class})
class BristlebudFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two Food tokens")
    void createsTwoFoodTokensOnEntry() {
        castAndResolve();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking may sacrifice a Food to mill three cards and return a milled permanent")
    void sacrificesFoodToMillAndReturnPermanent() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new Forest(), new Opt(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Food"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack trigger keeps the Food and library unchanged")
    void declinesFoodSacrifice() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new Forest(), new Opt(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BristlebudFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        findPermanent(player1, "Bristlebud Farmer").setSummoningSick(false);
    }
}
