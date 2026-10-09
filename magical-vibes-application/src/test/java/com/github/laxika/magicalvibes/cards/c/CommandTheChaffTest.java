package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HelmOfAwakening;
import com.github.laxika.magicalvibes.cards.s.SilvergillAdept;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandTheChaff.class, GrizzlyBears.class, Forest.class, SilvergillAdept.class,
        HelmOfAwakening.class, SphereOfResistance.class})
class CommandTheChaffTest extends BaseCardTest {

    @Test
    @DisplayName("Offers nonland cards from the targeted opponent's sideboard")
    void castsCardFromTargetOpponentsSideboard() {
        Card chosen = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        Card casterSideboardCard = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(casterSideboardCard)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(chosen, remaining)));

        CommandTheChaff command = castCommandTheChaff();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(casterSideboardCard);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(remaining);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("Declining leaves the targeted sideboard unchanged and exiles Command the Chaff")
    void decliningLeavesTargetSideboardUnchanged() {
        Card sideboardCard = new GrizzlyBears();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardCard)));

        CommandTheChaff command = castCommandTheChaff();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("Does not offer lands from the targeted sideboard")
    void doesNotOfferLandCards() {
        Forest forest = new Forest();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(forest)));

        CommandTheChaff command = castCommandTheChaff();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("Can decline the first card and cast a later card, retaining its opponent owner")
    void castsLaterCardAfterDecliningFirst() {
        Card first = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        first.setOwnerId(player2.getId());
        chosen.setOwnerId(player2.getId());
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(first, chosen)));

        CommandTheChaff command = castCommandTheChaff();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == chosen);
        assertThat(chosen.getOwnerId()).isEqualTo(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("An empty sideboard still exiles Command the Chaff")
    void emptySideboardStillExilesCommand() {
        gd.playerSideboards.put(player2.getId(), new ArrayList<>());

        CommandTheChaff command = castCommandTheChaff();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("The caster gets to look at lands in the opponent's sideboard")
    void showsLandCardsInSideboardToCaster() {
        Forest forest = new Forest();
        forest.setOwnerId(player2.getId());
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(forest)));
        harness.clearMessages();

        castCommandTheChaff();

        assertThat(harness.getConn1().getMessagesContaining(forest.getId().toString())).isNotEmpty();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Cannot cast Silvergill Adept without paying or revealing for its additional cost")
    void mandatoryAdditionalCostIsNotWaived() {
        SilvergillAdept adept = new SilvergillAdept();
        adept.setOwnerId(player2.getId());
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(adept)));

        castCommandTheChaff();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(adept);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == adept);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == adept);
    }

    @Test
    void canPayMandatoryAdditionalManaCost() {
        SilvergillAdept adept = new SilvergillAdept();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(adept)));
        castCommandTheChaff();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerSideboards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == adept);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void appliesCostReductionsAndTaxesToAdditionalManaCost() {
        SilvergillAdept adept = new SilvergillAdept();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(adept)));
        castCommandTheChaff();
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerSideboards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == adept);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canRevealMatchingHandCardForMandatoryAdditionalCost() {
        SilvergillAdept adept = new SilvergillAdept();
        SilvergillAdept reveal = new SilvergillAdept();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(adept)));
        castCommandTheChaff();
        harness.setHand(player1, List.of(reveal));
        harness.clearMessages();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerSideboards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == adept);
        harness.assertInHand(player1, "Silvergill Adept");
        assertThat(harness.getConn2().getMessagesContaining(reveal.getId().toString())).isNotEmpty();
    }

    private CommandTheChaff castCommandTheChaff() {
        CommandTheChaff command = new CommandTheChaff();
        harness.setHand(player1, List.of(command));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        return command;
    }
}
