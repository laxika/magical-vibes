package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMannequin;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({KamiOfTransience.class, HonorOfThePure.class, AuraOfSilence.class, MindStone.class,
        Murder.class, GrizzlyBears.class, WrathOfGod.class, MakeshiftMannequin.class})
class KamiOfTransienceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment spell puts a +1/+1 counter on Kami of Transience")
    void gainsCounterWhenEnchantmentIsCast() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTransience());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At each end step, Kami may return after an enchantment entered its owner's graveyard")
    void returnsFromGraveyardAfterEnchantmentWasPutThere() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTransience());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.sacrificePermanent(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, kami.getId());

        harness.assertInGraveyard(player1, "Kami of Transience");
        advanceToEndStep(player2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of Transience");
        harness.assertNotInGraveyard(player1, "Kami of Transience");
    }

    @Test
    @DisplayName("Does not trigger when only a non-enchantment permanent entered a graveyard")
    void doesNotTriggerForNonEnchantmentPermanent() {
        harness.addToBattlefield(player1, new KamiOfTransience());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Kami of Transience");
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineReturnAtOwnEndStep() {
        harness.setGraveyard(player1, List.of(new KamiOfTransience()));
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.sacrificePermanent(player1, 0, artifact.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kami of Transience");
        harness.assertNotInHand(player1, "Kami of Transience");
    }

    @Test
    void opponentsEnchantmentInTheirGraveyardDoesNotEnableReturn() {
        harness.setGraveyard(player1, List.of(new KamiOfTransience()));
        harness.addToBattlefield(player2, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.sacrificePermanent(player2, 0, artifact.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kami of Transience");
    }

    @Test
    void enchantmentEnteringWithoutBeingCastDoesNotGiveCounter() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTransience());

        harness.enterBattlefieldAndReturn(player1, new HonorOfThePure());

        assertThat(gd.stack).isEmpty();
        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsEnchantmentSpellDoesNotGiveCounter() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTransience());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HonorOfThePure()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingNonEnchantmentDoesNotGiveCounter() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTransience());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void oldEndStepTriggerDoesNotReturnKamiAfterItLeavesAndReentersGraveyard() {
        KamiOfTransience kami = new KamiOfTransience();
        harness.setGraveyard(player1, List.of(kami));
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.sacrificePermanent(player1, 0, artifact.getId());
        harness.passBothPriorities();
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new MakeshiftMannequin(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveInstant(player1, 0, kami.getId());
        harness.assertOnBattlefield(player1, "Kami of Transience");
        Permanent returnedKami = findPermanent(player1, "Kami of Transience");
        harness.castInstant(player1, 0, returnedKami.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Kami of Transience");

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Kami of Transience");
        harness.assertNotInHand(player1, "Kami of Transience");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
