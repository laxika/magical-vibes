package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JusticiarsPortal;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianProject.class, SauroformHybrid.class, Mortify.class,
        Scorchmark.class, JusticiarsPortal.class, GoblinGathering.class})
class GuardianProjectTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for an entering creature with a unique name")
    void drawsForUniqueName() {
        addGuardian(player1);
        castHybrid(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when another controlled creature has the same name")
    void doesNotTriggerForSameNameOnBattlefield() {
        addGuardian(player1);
        harness.addToBattlefield(player1, new SauroformHybrid());
        castHybrid(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a creature card in the graveyard has the same name")
    void doesNotTriggerForSameNameInGraveyard() {
        addGuardian(player1);
        harness.setGraveyard(player1, List.of(new SauroformHybrid()));
        castHybrid(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the same-name condition when the trigger resolves")
    void doesNotDrawIfEnteringCreatureDiesBeforeResolution() {
        addGuardian(player1);
        Card entering = new SauroformHybrid();
        harness.setHand(player1, List.of(entering));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID enteringPermanentId = harness.getPermanentId(player1, "Sauroform Hybrid");
        harness.setHand(player2, List.of(new Mortify()));
        addMortifyMana(player2);
        harness.castAndResolveInstant(player2, 0, enteringPermanentId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(entering.getId()));
    }

    @Test
    void matchingBattlefieldNamePreventsAbilityFromGoingOnStack() {
        addGuardian(player1);
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void matchingGraveyardNamePreventsAbilityFromGoingOnStack() {
        addGuardian(player1);
        harness.setGraveyard(player1, List.of(new SauroformHybrid()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawIfAnotherSameNamedCreatureAppearsBeforeResolution() {
        addGuardian(player1);
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void blinkedCreatureOnlyDrawsForItsNewEntry() {
        addGuardian(player1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Sauroform Hybrid")).isEqualTo(1);
    }

    @Test
    void drawsIfEnteringCreatureIsExiledBeforeResolution() {
        addGuardian(player1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, entering.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Sauroform Hybrid");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(entering.getCard().getId()));
    }

    @Test
    void opponentSameNamedCreaturesAndGraveyardCardsDoNotPreventDraw() {
        addGuardian(player1);
        harness.addToBattlefield(player2, new SauroformHybrid());
        harness.setGraveyard(player2, List.of(new SauroformHybrid()));

        castHybrid(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentCreatureEntryDoesNotTrigger() {
        addGuardian(player1);
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new SauroformHybrid());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void creatureTokensDoNotTrigger() {
        addGuardian(player1);
        harness.setHand(player1, List.of(new GoblinGathering()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void abilityStillDrawsAfterGuardianProjectIsDestroyed() {
        addGuardian(player1);
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player2, List.of(new Mortify()));
        addMortifyMana(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Guardian Project"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Guardian Project");
    }

    private void castHybrid(Player player) {
        harness.setHand(player, List.of(new SauroformHybrid()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addGuardian(Player player) {
        harness.addToBattlefield(player, new GuardianProject());
    }

    private void addMortifyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
