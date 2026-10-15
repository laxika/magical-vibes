package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.a.ArtfulManeuver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfoundJourney.class, DromokaWarrior.class, ArtfulManeuver.class, Pacifism.class})
class ProfoundJourneyTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentFromGraveyardToBattlefield() {
        Card target = new DromokaWarrior();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dromoka Warrior");
        harness.assertNotInGraveyard(player1, "Dromoka Warrior");
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Card target = new ArtfulManeuver();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reboundOffersAnotherCastAtNextUpkeep() {
        Card firstTarget = new DromokaWarrior();
        Card secondTarget = new DromokaWarrior();
        ProfoundJourney card = new ProfoundJourney();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        harness.setHand(player1, List.of(card));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Profound Journey");
    }

    @Test
    void cannotTargetAnOpponentsPermanentCard() {
        Card target = new DromokaWarrior();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalTargetPreventsRebound() {
        Card target = new DromokaWarrior();
        ProfoundJourney card = new ProfoundJourney();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(card));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Profound Journey");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertNotOnBattlefield(player1, "Dromoka Warrior");
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOffer() {
        Card firstTarget = new DromokaWarrior();
        Card secondTarget = new DromokaWarrior();
        ProfoundJourney card = new ProfoundJourney();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        harness.setHand(player1, List.of(card));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondTarget);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returningAnAuraOffersALegalAttachmentChoice() {
        Pacifism aura = new Pacifism();
        harness.addToBattlefield(player2, new DromokaWarrior());
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        var hostId = harness.getPermanentId(player2, "Dromoka Warrior");
        harness.handlePermanentChosen(player1, hostId);

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(hostId);
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void auraWithNoLegalAttachmentRemainsInGraveyard() {
        Pacifism aura = new Pacifism();
        ProfoundJourney card = new ProfoundJourney();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(card));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private void addProfoundJourneyMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
