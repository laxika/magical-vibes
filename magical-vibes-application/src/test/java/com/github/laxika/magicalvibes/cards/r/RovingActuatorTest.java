package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RovingActuator.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class RovingActuatorTest extends BaseCardTest {

    @Test
    void voidEtbCopiesAndCastsTargetedSpell() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card shock = new Shock();
        Card invalidManaValue = new CounselOfTheSoratami();
        Card invalidType = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, invalidManaValue, invalidType));
        harness.setHand(player1, List.of(new RovingActuator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(invalidManaValue, invalidType);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void voidEtbMayBeDeclinedWithoutChoosingATarget() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new RovingActuator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void voidEtbWithNoValidTargetStillResolves() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card invalidTarget = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(invalidTarget));
        harness.setHand(player1, List.of(new RovingActuator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalidTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void etbDoesNotTriggerWithoutVoid() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new RovingActuator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
