package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrevasRuins.class, TerminalMoraine.class, AlphaKavu.class})
class TrevasRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps the source")
    void acceptsEtbCostByReturningNonLairLand() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        TrevasRuins ruins = new TrevasRuins();
        playAndResolveEtb(ruins);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ruins)
                .doesNotContain(moraine);
        assertThat(gd.playerHands.get(player1.getId())).contains(moraine.getCard());
    }

    @Test
    @DisplayName("The source is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new TrevasRuins());
        TrevasRuins enteringRuins = playAndResolveEtb(new TrevasRuins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(existingLair)
                .noneMatch(permanent -> permanent.getCard() == enteringRuins);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enteringRuins)
                .noneMatch(card -> card == existingLair.getCard());
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices the source")
    void decliningEtbCostSacrificesSource() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        TrevasRuins ruins = new TrevasRuins();
        playAndResolveEtb(ruins);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ruins)
                .anyMatch(permanent -> permanent == moraine);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ruins);
    }

    @Test
    @DisplayName("The source is sacrificed when no land is available to return")
    void sacrificesWhenNoLandIsAvailable() {
        TrevasRuins ruins = playAndResolveEtb(new TrevasRuins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ruins);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ruins);
    }

    @Test
    @DisplayName("Accepting the ETB cost with multiple eligible lands returns only the chosen land")
    void acceptsEtbCostWithMultipleEligibleLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        TrevasRuins ruins = playAndResolveEtb(new TrevasRuins());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondLand)
                .doesNotContain(firstLand)
                .anyMatch(permanent -> permanent.getCard() == ruins);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstLand.getCard())
                .doesNotContain(secondLand.getCard());
    }

    @Test
    @DisplayName("An opponent's non-Lair land cannot pay the ETB cost")
    void ignoresOpponentControlledNonLairLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        TrevasRuins ruins = playAndResolveEtb(new TrevasRuins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ruins);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ruins);
        harness.assertOnBattlefield(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("A non-Lair nonland permanent cannot pay the ETB cost")
    void ignoresNonLandPermanents() {
        harness.addToBattlefield(player1, new AlphaKavu());
        TrevasRuins ruins = playAndResolveEtb(new TrevasRuins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ruins);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ruins);
        harness.assertOnBattlefield(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("The mana ability offers green, white, and blue")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new TrevasRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps the source")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new TrevasRuins());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(ruins.isTapped()).isTrue();
    }

    private TrevasRuins playAndResolveEtb(TrevasRuins ruins) {
        harness.setHand(player1, List.of(ruins));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return ruins;
    }
}
