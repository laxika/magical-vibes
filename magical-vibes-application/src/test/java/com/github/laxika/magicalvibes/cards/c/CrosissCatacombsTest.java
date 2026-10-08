package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarigaazsCaldera;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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

    @Test
    @DisplayName("A tapped non-Lair land can pay the ETB cost")
    void returnsTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        land.tap();
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Terminal Moraine");
        harness.assertNotOnBattlefield(player1, "Terminal Moraine");
        harness.assertOnBattlefield(player1, "Crosis's Catacombs");
    }

    @Test
    @DisplayName("With no other lands, Crosis's Catacombs sacrifices itself")
    void sacrificesWithNoOtherLands() {
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Crosis's Catacombs");
        harness.assertInGraveyard(player1, "Crosis's Catacombs");
    }

    @Test
    @DisplayName("A controlled land returns to its owner's hand")
    void returnsLandToOpponentOwner() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        gd.stolenCreatures.put(land.getId(), player2.getId());
        recordControlEffect(land, player1);
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Terminal Moraine");
        harness.assertNotInHand(player1, "Terminal Moraine");
        harness.assertNotOnBattlefield(player1, "Terminal Moraine");
        harness.assertOnBattlefield(player1, "Crosis's Catacombs");
    }

    @Test
    @DisplayName("The land can produce mana before its ETB trigger sacrifices it")
    void producesManaBeforeEtbSacrifice() {
        harness.setHand(player1, List.of(new CrosissCatacombs()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Crosis's Catacombs");
        harness.assertNotOnBattlefield(player1, "Crosis's Catacombs");
    }

    @Test
    @DisplayName("Choosing black adds exactly one black mana")
    void choosingBlackAddsOnlyBlackMana() {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new CrosissCatacombs());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(catacombs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private CrosissCatacombs playAndResolveEtb() {
        CrosissCatacombs catacombs = new CrosissCatacombs();
        harness.setHand(player1, List.of(catacombs));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return catacombs;
    }
    private void recordControlEffect(Permanent permanent, com.github.laxika.magicalvibes.model.Player controller) {
        gd.addFloatingEffect(new FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Control setup", null, controller.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
