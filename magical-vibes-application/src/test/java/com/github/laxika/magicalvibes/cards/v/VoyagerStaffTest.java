package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoyagerStaff.class, BorosRecruit.class, BorosSignet.class})
class VoyagerStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and exiles the target creature until the next end step")
    void sacrificesAndExilesTargetCreature() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        BorosRecruit recruitCard = new BorosRecruit();
        recruitCard.setOwnerId(player2.getId());
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, recruitCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID recruitPermanentId = recruit.getId();
        UUID recruitCardId = recruit.getCard().getId();
        harness.activateAbility(player1, 0, null, recruitPermanentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Voyager Staff");
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(recruitCardId));
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(action -> action.card().getId().equals(recruitCardId));
    }

    @Test
    @DisplayName("Returns the exiled creature at the next end step under its owner's control")
    void returnsAtEndStep() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        BorosRecruit recruitCard = new BorosRecruit();
        recruitCard.setOwnerId(player1.getId());
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, recruitCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID recruitPermanentId = recruit.getId();
        UUID recruitCardId = recruit.getCard().getId();
        gd.stolenCreatures.put(recruitPermanentId, player1.getId());
        harness.activateAbility(player1, 0, null, recruitPermanentId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(recruitCardId));

        harness.forceActivePlayer(player1);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boros Recruit");
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(recruitCardId));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        harness.addToBattlefield(player2, new BorosSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID signetId = harness.getPermanentId(player2, "Boros Signet");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, signetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two generic mana to activate")
    void requiresTwoGenericMana() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, recruit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Staff is sacrificed as a cost before its ability resolves")
    void tappedStaffPaysSacrificeBeforeResolution() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new VoyagerStaff());
        staff.tap();
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, recruit.getId());

        harness.assertNotOnBattlefield(player1, "Voyager Staff");
        harness.assertInGraveyard(player1, "Voyager Staff");
        harness.assertOnBattlefield(player2, "Boros Recruit");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Activation during an end step waits until the following turn's end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, recruit.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Boros Recruit");

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("The delayed return cannot find a card that left exile and was exiled again")
    void doesNotReturnCardThatChangedZonesBeforeEndStep() {
        harness.addToBattlefield(player1, new VoyagerStaff());
        BorosRecruit recruitCard = new BorosRecruit();
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, recruitCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, recruit.getId());
        harness.passBothPriorities();

        assertThat(gd.removeFromExile(recruitCard.getId())).isTrue();
        harness.setHand(player1, List.of(recruitCard));
        harness.setHand(player1, List.of());
        harness.setExile(player1, List.of(recruitCard));

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(recruitCard.getId()));
    }
}
