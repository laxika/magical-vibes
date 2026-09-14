package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DarigaazsCaldera.class, TerminalMoraine.class})
class DarigaazsCalderaTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Darigaaz's Caldera")
    void acceptsEtbCostByReturningNonLairLand() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DarigaazsCaldera enteringCaldera = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == enteringCaldera)
                .noneMatch(permanent -> permanent == moraine);
        assertThat(gd.playerHands.get(player1.getId())).contains(moraine.getCard());
    }

    @Test
    @DisplayName("Darigaaz's Caldera is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new DarigaazsCaldera());
        DarigaazsCaldera enteringCaldera = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(existingLair)
                .noneMatch(permanent -> permanent.getCard() == enteringCaldera);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enteringCaldera)
                .noneMatch(card -> card == existingLair.getCard());
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Darigaaz's Caldera")
    void decliningEtbCostSacrificesSource() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DarigaazsCaldera enteringCaldera = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(moraine)
                .noneMatch(permanent -> permanent.getCard() == enteringCaldera);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCaldera);
    }

    @Test
    @DisplayName("Accepting the ETB cost with multiple eligible lands returns only the chosen land")
    void acceptsEtbCostWithMultipleEligibleLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DarigaazsCaldera enteringCaldera = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondLand)
                .noneMatch(permanent -> permanent == firstLand)
                .anyMatch(permanent -> permanent.getCard() == enteringCaldera);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstLand.getCard())
                .doesNotContain(secondLand.getCard());
    }

    @Test
    @DisplayName("An opponent's non-Lair land cannot pay the ETB cost")
    void ignoresOpponentControlledNonLairLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        DarigaazsCaldera enteringCaldera = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringCaldera);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCaldera);
        harness.assertOnBattlefield(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("The mana ability offers black, red, and green")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new DarigaazsCaldera());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "GREEN", "RED");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Darigaaz's Caldera")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent caldera = harness.addToBattlefieldAndReturn(player1, new DarigaazsCaldera());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(caldera.isTapped()).isTrue();
    }

    private DarigaazsCaldera playAndResolveEtb() {
        DarigaazsCaldera caldera = new DarigaazsCaldera();
        harness.setHand(player1, List.of(caldera));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return caldera;
    }
}
