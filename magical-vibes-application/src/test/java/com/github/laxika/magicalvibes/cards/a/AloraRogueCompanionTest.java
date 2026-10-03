package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AloraRogueCompanion.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class, Unsummon.class})
class AloraRogueCompanionTest extends BaseCardTest {

    @Test
    void attackingMakesTheChosenAttackerUnblockableAndReturnsItAtNextEndStep() {
        addAloraReady();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(attacker);

        assertThat(attacker.isCantBeBlocked()).isTrue();
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(attacker.getCard().getId());
    }

    @Test
    void whiteFaceCreatesASoldierAfterTheAttackerReturns() {
        specialize(0, new Plains());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    void blackFaceMakesEachOpponentLoseTwoLifeAfterTheAttackerReturns() {
        specialize(2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        harness.assertLife(player2, 16);
    }

    @Test
    void redFaceCreatesATreasureAfterTheAttackerReturns() {
        specialize(3, new Mountain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void greenFaceReturnsTheAttackerWithAPerpetualBoost() {
        specialize(4, new Forest());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(attacker.getCard().getId(),
                        new com.github.laxika.magicalvibes.model.CardPowerToughnessModifier(1, 1));
    }

    @Test
    void blueFaceLetsTheControllerChooseAnOpponentCreatureForAPerpetualMinusOnePower() {
        specialize(1, new Island());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new GrizzlyBears());

        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstOpponentCreature.getId(), secondOpponentCreature.getId());
        harness.handlePermanentChosen(player1, secondOpponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, secondOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(2);
    }

    @Test
    void choosingNoAttackerDoesNotReturnAloraOrCreateAToken() {
        Permanent alora = specialize(0, new Plains());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alora, attacker);
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void delayedReturnStillHappensAfterAloraLeavesTheBattlefield() {
        Permanent alora = addAloraReady();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attackWithAlora(attacker);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, alora.getId());
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(alora, attacker);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(alora.getCard().getId(), attacker.getCard().getId());
    }

    @Test
    void blackFaceDoesNotCauseLifeLossWhenTheAttackerAlreadyLeft() {
        specialize(2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attackWithAlora(attacker);
        int lifeAfterCombat = gd.getLife(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        returnAttackerAtEndStep();

        harness.assertLife(player2, lifeAfterCombat);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(attacker.getCard().getId());
    }

    @Test
    void specializedAloraKeepsItsTokenAbilityAfterReturningToHandAndBeingRecast() {
        Permanent alora = specialize(0, new Plains());
        alora.setSummoningSick(false);
        attackWithAlora(alora);
        returnAttackerAtEndStep();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(alora.getCard().getId()))).isTrue();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attackWithAlora(attacker);
        returnAttackerAtEndStep();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
    }

    private Permanent addAloraReady() {
        return addCreatureReady(player1, new AloraRogueCompanion());
    }

    private Permanent specialize(int abilityIndex, Card discard) {
        harness.setHand(player1, List.of(new AloraRogueCompanion(), discard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void attackWithAlora(Permanent attacker) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
    }

    private void returnAttackerAtEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
