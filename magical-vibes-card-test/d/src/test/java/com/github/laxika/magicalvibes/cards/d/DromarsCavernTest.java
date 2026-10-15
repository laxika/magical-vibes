package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromarsCavern.class, TerminalMoraine.class, AlphaKavu.class})
class DromarsCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Dromar's Cavern")
    void acceptsEtbCostByReturningNonLairLand() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DromarsCavern enteringCavern = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == enteringCavern)
                .noneMatch(permanent -> permanent == moraine);
        assertThat(gd.playerHands.get(player1.getId())).contains(moraine.getCard());
    }

    @Test
    @DisplayName("Dromar's Cavern is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new DromarsCavern());
        DromarsCavern enteringCavern = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(existingLair)
                .noneMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enteringCavern)
                .noneMatch(card -> card == existingLair.getCard());
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Dromar's Cavern")
    void decliningEtbCostSacrificesSource() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DromarsCavern enteringCavern = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(moraine)
                .noneMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCavern);
    }

    @Test
    @DisplayName("Dromar's Cavern is sacrificed when no land is available to return")
    void sacrificesWhenNoLandIsAvailable() {
        DromarsCavern enteringCavern = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCavern);
    }

    @Test
    @DisplayName("Accepting the ETB cost with multiple eligible lands returns only the chosen land")
    void acceptsEtbCostWithMultipleEligibleLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        DromarsCavern enteringCavern = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondLand)
                .noneMatch(permanent -> permanent == firstLand)
                .anyMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstLand.getCard())
                .doesNotContain(secondLand.getCard());
    }

    @Test
    @DisplayName("An opponent's non-Lair land cannot pay the ETB cost")
    void ignoresOpponentControlledNonLairLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        DromarsCavern enteringCavern = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCavern);
        harness.assertOnBattlefield(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("A non-Lair nonland permanent cannot pay the ETB cost")
    void ignoresNonLandPermanents() {
        harness.addToBattlefield(player1, new AlphaKavu());
        DromarsCavern enteringCavern = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringCavern);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringCavern);
        harness.assertOnBattlefield(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("The mana ability offers white, blue, and black")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Dromar's Cavern")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(cavern.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    @DisplayName("The other offered colors each produce exactly one mana immediately")
    void producesOtherOfferedManaColors(ManaColor color) {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        for (ManaColor other : ManaColor.values()) {
            if (other != color) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(other)).isZero();
            }
        }
        assertThat(cavern.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dromar's Cavern can produce mana before its entry trigger sacrifices it")
    void producesManaBeforeEntryTriggerResolves() {
        DromarsCavern cavern = new DromarsCavern();
        harness.setHand(player1, List.of(cavern));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dromar's Cavern");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cavern);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped non-Lair land can be returned while another Lair remains")
    void returnsTappedLandAmongLairs() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new DromarsCavern());
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        moraine.tap();
        DromarsCavern cavern = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(existingLair)
                .doesNotContain(moraine)
                .anyMatch(permanent -> permanent.getCard() == cavern);
        assertThat(gd.playerHands.get(player1.getId())).contains(moraine.getCard());
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to its owner's hand")
    void returnsControlledLandToOpponentsHand() {
        TerminalMoraine moraine = new TerminalMoraine();
        moraine.setOwnerId(player2.getId());
        Permanent controlledLand = harness.addToBattlefieldAndReturn(player1, moraine);
        DromarsCavern cavern = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(controlledLand)
                .anyMatch(permanent -> permanent.getCard() == cavern);
        assertThat(gd.playerHands.get(player2.getId())).contains(moraine);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(moraine);
    }

    private DromarsCavern playAndResolveEtb() {
        DromarsCavern cavern = new DromarsCavern();
        harness.setHand(player1, List.of(cavern));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return cavern;
    }
}
