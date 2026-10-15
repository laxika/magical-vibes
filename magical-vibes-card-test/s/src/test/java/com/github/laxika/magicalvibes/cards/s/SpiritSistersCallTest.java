package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritSistersCall.class, GrizzlyBears.class, LightningBolt.class, TamiyosCompleation.class})
class SpiritSistersCallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen permanent card after sacrificing a permanent with a shared card type")
    void returnsChosenPermanentCardAfterMatchingSacrifice() {
        Permanent call = harness.addToBattlefieldAndReturn(player1, new SpiritSistersCall());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(call.getCard().getId()));
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isExileIfLeavesBattlefield()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
    }

    @Test
    @DisplayName("Can be declined without sacrificing or returning a card")
    void canBeDeclined() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("Exiles the returned permanent when it would leave the battlefield")
    void returnedPermanentIsExiledIfItLeavesBattlefield() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Only permanent cards in the controller's graveyard are offered as targets")
    void onlyOwnPermanentCardsAreTargets() {
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Card chosen = new GrizzlyBears();
        Card instant = new LightningBolt();
        Card opposingCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, instant));
        harness.setGraveyard(player2, List.of(opposingCard));

        moveToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(chosen);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Only controlled permanents sharing a card type can be sacrificed")
    void sacrificeChoicesRequireControlAndSharedCardType() {
        Permanent call = harness.addToBattlefieldAndReturn(player1, new SpiritSistersCall());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(call);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return the target when there is no matching permanent to sacrifice")
    void cannotReturnWithoutMatchingSacrifice() {
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosen);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can sacrifice Spirit-Sister's Call itself to return an enchantment")
    void canSacrificeSourceToReturnEnchantment() {
        Permanent call = harness.addToBattlefieldAndReturn(player1, new SpiritSistersCall());
        Card chosen = new SpiritSistersCall();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, call.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(call);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(chosen.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(call.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later Tamiyo's Compleation removes the gained exile replacement ability")
    void laterAbilityRemovalAllowsReturnedCreatureToDie() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        moveToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        Permanent returned = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new TamiyosCompleation(), new LightningBolt()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castEnchantment(player2, 0, returned.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(chosen);
    }

    @Test
    @DisplayName("Does not trigger at the beginning of an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card chosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("An empty graveyard leaves no legal target and does not prompt a sacrifice")
    void emptyGraveyardDoesNotPromptSacrifice() {
        harness.addToBattlefield(player1, new SpiritSistersCall());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());

        moveToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
