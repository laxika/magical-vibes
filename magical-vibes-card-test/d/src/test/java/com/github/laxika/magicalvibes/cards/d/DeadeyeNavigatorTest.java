package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadeyeNavigator.class, GrizzlyBears.class, ImprisonedInTheMoon.class})
class DeadeyeNavigatorTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new DeadeyeNavigator(), "{4}{U}{U}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    @Test
    @DisplayName("Soulbond ETB pairs Deadeye with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");

        assertThat(navigator.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(navigator.getId());
    }

    @Test
    @DisplayName("While paired, Deadeye can flicker itself")
    void pairedNavigatorCanFlickerSelf() {
        castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        UUID oldId = navigator.getId();

        harness.addMana(player1, ManaColor.BLUE, 2);
        int navIndex = gd.playerBattlefields.get(player1.getId()).indexOf(navigator);
        harness.activateAbility(player1, navIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Deadeye Navigator");
        assertThat(returned.getId()).isNotEqualTo(oldId);
        // Exile breaks the pair immediately.
        assertThat(returned.getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("While paired, the partner can flicker itself")
    void pairedPartnerCanFlickerSelf() {
        Permanent bears = castAndPairWithBears();
        UUID oldBearsId = bears.getId();

        harness.addMana(player1, ManaColor.BLUE, 2);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        harness.activateAbility(player1, bearsIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.getId()).isNotEqualTo(oldBearsId);
        assertThat(returnedBears.getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Unpaired Deadeye does not have the flicker ability")
    void unpairedCannotFlicker() {
        harness.addToBattlefield(player1, new DeadeyeNavigator());
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.addMana(player1, ManaColor.BLUE, 2);

        int navIndex = gd.playerBattlefields.get(player1.getId()).indexOf(navigator);
        assertThatThrownBy(() -> harness.activateAbility(player1, navIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired")
    void decliningLeavesUnpaired() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new DeadeyeNavigator(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        assertThat(navigator.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("An unpaired Navigator may pair with a creature that enters later")
    void pairsWithLaterEnteringCreature() {
        Permanent navigator = harness.addToBattlefieldAndReturn(player1, new DeadeyeNavigator());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(navigator.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(navigator.getId());
    }

    @Test
    @DisplayName("Pairing with a later entering creature can be declined")
    void declinesLaterEnteringCreature() {
        Permanent navigator = harness.addToBattlefieldAndReturn(player1, new DeadeyeNavigator());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(navigator.getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Grizzly Bears").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("A blinked partner can pair with Navigator again on returning")
    void blinkedPartnerCanReunitePair() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(navigator.getPairedWithId()).isEqualTo(returned.getId());
        assertThat(returned.getPairedWithId()).isEqualTo(navigator.getId());
    }

    @Test
    @DisplayName("A blinked Navigator can pair with its former partner again")
    void blinkedNavigatorCanReunitePair() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(navigator), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent returned = findPermanent(player1, "Deadeye Navigator");
        assertThat(returned.getId()).isNotEqualTo(navigator.getId());
        assertThat(returned.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Both creatures may blink if their abilities are activated before the pair breaks")
    void bothActivatedAbilitiesResolveAfterPairBreaks() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), 0, null, null);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(navigator), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Deadeye Navigator").getId()).isNotEqualTo(navigator.getId());
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
        assertThat(findPermanent(player1, "Deadeye Navigator").getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Grizzly Bears").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Turning the partner into a land breaks soulbond and removes Navigator's blink ability")
    void partnerCeasingToBeCreatureBreaksPair() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(navigator.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(navigator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Soulbond does not trigger when Navigator enters without another unpaired creature")
    void noPartnerMeansNoSoulbondTrigger() {
        harness.castFromHand(player1, new DeadeyeNavigator(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Deadeye Navigator").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("An already paired Navigator does not trigger soulbond for another entering creature")
    void pairedNavigatorDoesNotOfferNewPartner() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = findPermanent(player1, "Deadeye Navigator");
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(navigator.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(navigator.getId());
    }
}
