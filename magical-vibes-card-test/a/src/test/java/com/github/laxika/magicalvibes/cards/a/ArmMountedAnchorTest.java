package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudPirates;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmMountedAnchor.class, CloudPirates.class, GrizzlyBears.class})
class ArmMountedAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1);
        Permanent anchor = addAnchorReady(player1);
        anchor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage draws two cards and offers a Pirate discard")
    void combatDamageDrawsAndOffersPirateDiscard() {
        Permanent attacker = addCreatureReady(player1);
        Permanent anchor = addAnchorReady(player1);
        anchor.setAttachedTo(attacker.getId());
        CloudPirates pirate = new CloudPirates();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(pirate, new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        List<Card> hand = gd.playerHands.get(player1.getId());
        int pirateIndex = hand.stream().filter(card -> card.getSubtypes().contains(CardSubtype.PIRATE))
                .map(hand::indexOf).findFirst().orElseThrow();
        assertThat(discard.validIndices()).containsExactly(pirateIndex);
        assertThat(discard.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, pirateIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.PIRATE));
    }

    @Test
    @DisplayName("Combat damage without a Pirate forces discarding two cards")
    void combatDamageWithoutPirateDiscardsTwo() {
        Permanent attacker = addCreatureReady(player1);
        Permanent anchor = addAnchorReady(player1);
        anchor.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip costs zero with one or fewer cards in hand and two otherwise")
    void conditionalEquipCost() {
        Permanent anchor = addAnchorReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(anchor.getAttachedTo()).isEqualTo(creature.getId());

        anchor.setAttachedTo(null);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canDeclinePirateDiscardAndDiscardTwoNonPirates() {
        Permanent attacker = addCreatureReady(player1);
        addAnchorReady(player1).setAttachedTo(attacker.getId());
        CloudPirates pirate = new CloudPirates();
        harness.setHand(player1, List.of(pirate));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .remainingCount()).isEqualTo(2);
        int bearIndex = gd.playerHands.get(player1.getId()).indexOf(
                gd.playerHands.get(player1.getId()).stream()
                        .filter(GrizzlyBears.class::isInstance).findFirst().orElseThrow());
        harness.handleCardChosen(player1, bearIndex);
        bearIndex = gd.playerHands.get(player1.getId()).indexOf(
                gd.playerHands.get(player1.getId()).stream()
                        .filter(GrizzlyBears.class::isInstance).findFirst().orElseThrow());
        harness.handleCardChosen(player1, bearIndex);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pirate);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    void equipIsFreeWithExactlyOneCardInHand() {
        Permanent anchor = addAnchorReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(anchor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unattachedAnchorDoesNotTriggerForCombatDamage() {
        addCreatureReady(player1);
        addAnchorReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void equipmentControllerLootsWhenOpponentsEquippedCreatureDealsDamage() {
        Permanent attacker = addCreatureReady(player2);
        addAnchorReady(player1).setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private Permanent addAnchorReady(Player player) {
        Permanent anchor = harness.addToBattlefieldAndReturn(player, new ArmMountedAnchor());
        anchor.setSummoningSick(false);
        return anchor;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void resolveCombatAndTrigger() {
        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
    }
}
