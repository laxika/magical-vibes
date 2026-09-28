package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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
        Mountain.class, Plains.class, Swamp.class})
class AloraRogueCompanionTest extends BaseCardTest {

    @Test
    void attackingMakesTheChosenAttackerUnblockableAndReturnsItAtNextEndStep() {
        Permanent alora = addAloraReady();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(alora, attacker);

        assertThat(attacker.isCantBeBlocked()).isTrue();
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(attacker.getCard().getId());
    }

    @Test
    void whiteFaceCreatesASoldierAfterTheAttackerReturns() {
        Permanent alora = specialize(CardColor.WHITE, 0, new Plains());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(alora, attacker);
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(1);
    }

    @Test
    void blackFaceMakesEachOpponentLoseTwoLifeAfterTheAttackerReturns() {
        Permanent alora = specialize(CardColor.BLACK, 2, new Swamp());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(alora, attacker);
        returnAttackerAtEndStep();

        harness.assertLife(player2, 16);
    }

    @Test
    void redFaceCreatesATreasureAfterTheAttackerReturns() {
        Permanent alora = specialize(CardColor.RED, 3, new Mountain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(alora, attacker);
        returnAttackerAtEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Treasure"))
                .hasSize(1);
    }

    @Test
    void greenFaceReturnsTheAttackerWithAPerpetualBoost() {
        Permanent alora = specialize(CardColor.GREEN, 4, new Forest());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        attackWithAlora(alora, attacker);
        returnAttackerAtEndStep();

        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(attacker.getCard().getId(),
                        new com.github.laxika.magicalvibes.model.CardPowerToughnessModifier(1, 1));
    }

    @Test
    void blueFaceLetsTheControllerChooseAnOpponentCreatureForAPerpetualMinusOnePower() {
        Permanent alora = specialize(CardColor.BLUE, 1, new Island());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new GrizzlyBears());

        attackWithAlora(alora, attacker);
        returnAttackerAtEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstOpponentCreature.getId(), secondOpponentCreature.getId());
        harness.handlePermanentChosen(player1, secondOpponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, secondOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(2);
    }

    private Permanent addAloraReady() {
        return addCreatureReady(player1, new AloraRogueCompanion());
    }

    private Permanent specialize(CardColor color, int abilityIndex, Card discard) {
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

    private void attackWithAlora(Permanent alora, Permanent attacker) {
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
