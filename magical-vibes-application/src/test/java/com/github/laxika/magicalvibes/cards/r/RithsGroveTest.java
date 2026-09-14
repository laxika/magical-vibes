package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
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

@CardUsed({RithsGrove.class, TerminalMoraine.class, AlphaKavu.class})
class RithsGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps Rith's Grove")
    void acceptsEtbCostByReturningNonLairLand() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        RithsGrove enteringGrove = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == enteringGrove)
                .noneMatch(permanent -> permanent == moraine);
        assertThat(gd.playerHands.get(player1.getId())).contains(moraine.getCard());
    }

    @Test
    @DisplayName("Rith's Grove is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        Permanent existingLair = harness.addToBattlefieldAndReturn(player1, new RithsGrove());
        RithsGrove enteringGrove = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(existingLair)
                .noneMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enteringGrove)
                .noneMatch(card -> card == existingLair.getCard());
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices Rith's Grove")
    void decliningEtbCostSacrificesSource() {
        Permanent moraine = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        RithsGrove enteringGrove = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(moraine)
                .noneMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringGrove);
    }

    @Test
    @DisplayName("Rith's Grove is sacrificed when no land is available to return")
    void sacrificesWhenNoLandIsAvailable() {
        RithsGrove enteringGrove = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringGrove);
    }

    @Test
    @DisplayName("Accepting the ETB cost with multiple eligible lands returns only the chosen land")
    void acceptsEtbCostWithMultipleEligibleLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        RithsGrove enteringGrove = playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondLand)
                .noneMatch(permanent -> permanent == firstLand)
                .anyMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstLand.getCard())
                .doesNotContain(secondLand.getCard());
    }

    @Test
    @DisplayName("An opponent's non-Lair land cannot pay the ETB cost")
    void ignoresOpponentControlledNonLairLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        RithsGrove enteringGrove = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringGrove);
        harness.assertOnBattlefield(player2, "Terminal Moraine");
    }

    @Test
    @DisplayName("A non-Lair nonland permanent cannot pay the ETB cost")
    void ignoresNonLandPermanents() {
        harness.addToBattlefield(player1, new AlphaKavu());
        RithsGrove enteringGrove = playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == enteringGrove);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enteringGrove);
        harness.assertOnBattlefield(player1, "Alpha Kavu");
    }

    @Test
    @DisplayName("The mana ability offers red, green, and white")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new RithsGrove());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Rith's Grove")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new RithsGrove());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    private RithsGrove playAndResolveEtb() {
        RithsGrove grove = new RithsGrove();
        harness.setHand(player1, List.of(grove));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return grove;
    }
}
