package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParnesseTheSubtleBrush.class, Shock.class, CounselOfTheSoratami.class, Reverberate.class,
        ProdigalPyromancer.class, Fireball.class})
class ParnesseTheSubtleBrushTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell unless they pay four life")
    void countersOpponentSpellWithoutLifePayment() {
        Permanent parnesse = addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, parnesse.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lets a chosen opponent copy the copied spell")
    void chosenOpponentCopiesCopiedSpell() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new Reverberate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }

    @Test
    void opponentCanDeclineToCopy() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new Reverberate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    void controllerCanChooseNoOpponent() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new Reverberate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void opponentCanPayLifeToTargetController() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 18);
    }

    @Test
    void protectsAnotherControlledPermanent() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, pyromancer.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prodigal Pyromancer");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void countersWithoutOfferingUnaffordableLifePayment() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setLife(player2, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 3);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllerSpellsDoNotRequireLifePayment() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersOpponentActivatedAbility() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void eachProtectedTargetRequiresASeparateLifePayment() {
        Permanent parnesse = addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player2, 0, 2, List.of(player1.getId(), parnesse.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 19);
        assertThat(parnesse.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void opponentMayChangeTargetsOfAMultipleTargetCopy() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        Permanent second = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new Fireball(), new Reverberate()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 2, List.of(player2.getId(), second.getId()));
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void opposingCopyWithUnchangedTargetsRequiresLifePayment() {
        addCreatureReady(player1, new ParnesseTheSubtleBrush());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, gd.stack.getLast().getCard().getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
