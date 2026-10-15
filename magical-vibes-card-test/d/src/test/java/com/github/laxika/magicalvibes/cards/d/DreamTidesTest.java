package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.u.UktabiOrangutan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamTides.class, CloudElemental.class, UktabiOrangutan.class, Quicksand.class, IvoryMask.class})
class DreamTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures stay tapped while noncreatures untap normally")
    void creaturesStayTappedThroughUntap() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent cloudElemental = addTapped(player1, new CloudElemental());
        Permanent orangutan = addTapped(player1, new UktabiOrangutan());
        Permanent quicksand = addTapped(player1, new Quicksand());

        harness.performUntapStep(player1);

        assertThat(cloudElemental.isTapped()).isTrue();
        assertThat(orangutan.isTapped()).isTrue();
        assertThat(quicksand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {2} untaps the chosen nongreen creature")
    void payingTwoUntapsChosenNongreenCreature() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent cloudElemental = addTapped(player1, new CloudElemental());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(cloudElemental.getId()));

        assertThat(cloudElemental.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Paying {4} untaps two chosen nongreen creatures")
    void payingFourUntapsTwoNongreenCreatures() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent cloudElementalA = addTapped(player1, new CloudElemental());
        Permanent cloudElementalB = addTapped(player1, new CloudElemental());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(cloudElementalA.getId(), cloudElementalB.getId()));

        assertThat(cloudElementalA.isTapped()).isFalse();
        assertThat(cloudElementalB.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("With only {1} available, no creature can be untapped")
    void insufficientManaLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent cloudElemental = addTapped(player1, new CloudElemental());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(cloudElemental.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped green creature is not offered by the upkeep trigger")
    void tappedGreenCreatureIsNotOfferedByUpkeepTrigger() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent orangutan = addTapped(player1, new UktabiOrangutan());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(orangutan.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the nongreen creature tapped")
    void choosingNoneLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent cloudElemental = addTapped(player1, new CloudElemental());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(cloudElemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each player's upkeep lets that player choose only their own nongreen creatures")
    void eachPlayerChoosesTheirOwnCreatures() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent player1Creature = addTapped(player1, new CloudElemental());
        Permanent player2Creature = addTapped(player2, new CloudElemental());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2Creature.getId())
                .doesNotContain(player1Creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Creature.getId()));

        assertThat(player2Creature.isTapped()).isFalse();
        assertThat(player1Creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only tapped nongreen creatures are offered, and a subset may be chosen")
    void choosesSubsetOfEligibleCreatures() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent chosen = addTapped(player1, new CloudElemental());
        Permanent unchosen = addTapped(player1, new CloudElemental());
        Permanent green = addTapped(player1, new UktabiOrangutan());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new CloudElemental());
        Permanent land = addTapped(player1, new Quicksand());

        advanceToUpkeep(player1);
        land.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId())));

        assertThat(chosen.isTapped()).isFalse();
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(green.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped lands can fund the payment during resolution with an empty mana pool")
    void manaAbilitiesCanFundPaymentDuringResolution() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent creature = addTapped(player1, new CloudElemental());
        harness.addToBattlefield(player1, new Quicksand());
        harness.addToBattlefield(player1, new Quicksand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        gs.activateAbility(gd, player1, 2, 0, null, null, null);
        gs.activateAbility(gd, player1, 3, 0, null, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The untap restriction applies to the opponent's creatures too")
    void opponentCreaturesStayTappedThroughUntap() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent creature = addTapped(player2, new CloudElemental());
        Permanent land = addTapped(player2, new Quicksand());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Player shroud does not prevent the nontargeting upkeep ability")
    void playerShroudDoesNotPreventUpkeepPayment() {
        harness.addToBattlefield(player1, new DreamTides());
        harness.addToBattlefield(player2, new IvoryMask());
        Permanent creature = addTapped(player2, new CloudElemental());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

}
