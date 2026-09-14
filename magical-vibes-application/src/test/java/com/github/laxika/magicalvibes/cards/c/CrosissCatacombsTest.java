package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarigaazsCaldera;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrosissCatacombs.class, DarigaazsCaldera.class, TerminalMoraine.class})
class CrosissCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Crosis's Catacombs")
    void acceptsEtbCostByReturningNonLairLand() {
        harness.addToBattlefield(player1, new TerminalMoraine());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Crosis's Catacombs");
        harness.assertNotOnBattlefield(player1, "Terminal Moraine");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Terminal Moraine"));
    }

    @Test
    @DisplayName("Crosis's Catacombs is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new DarigaazsCaldera());
        CrosissCatacombs enteringCatacombs = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent == existingLair)
                .noneMatch(permanent -> permanent.getCard() == enteringCatacombs);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == enteringCatacombs)
                .noneMatch(card -> card == existingLair.getCard());
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Crosis's Catacombs")
    void decliningEtbCostSacrificesSource() {
        harness.addToBattlefield(player1, new TerminalMoraine());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Crosis's Catacombs");
        harness.assertInGraveyard(player1, "Crosis's Catacombs");
        harness.assertOnBattlefield(player1, "Terminal Moraine");
    }

    @Test
    @DisplayName("Accepting the ETB cost with multiple eligible lands returns only the chosen land")
    void acceptsEtbCostWithMultipleEligibleLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        CrosissCatacombs enteringCatacombs = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent == secondLand)
                .noneMatch(permanent -> permanent == firstLand)
                .anyMatch(permanent -> permanent.getCard() == enteringCatacombs);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card == firstLand.getCard())
                .noneMatch(card -> card == secondLand.getCard());
    }

    @Test
    @DisplayName("An opponent's non-Lair land cannot pay the ETB cost")
    void ignoresOpponentControlledNonLairLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        CrosissCatacombs enteringCatacombs = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringCatacombs);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == enteringCatacombs);
        harness.assertOnBattlefield(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("The mana ability offers blue, black, and red")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new CrosissCatacombs());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Crosis's Catacombs")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new CrosissCatacombs());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(catacombs.isTapped()).isTrue();
    }

    private CrosissCatacombs playAndResolveEtb() {
        CrosissCatacombs catacombs = new CrosissCatacombs();
        harness.setHand(player1, List.of(catacombs));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return catacombs;
    }
}
