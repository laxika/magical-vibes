package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaperParasite.class})
class ShaperParasiteTest extends BaseCardTest {

    @Test
    void turningFaceUpCanGiveItPlusTwoMinusTwoUntilEndOfTurn() {
        Permanent parasite = castFaceDownAndTurnFaceUp();

        harness.handlePermanentChosen(player1, parasite.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        assertThat(gqs.getEffectivePower(gd, parasite)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, parasite)).isEqualTo(1);
    }

    @Test
    void turningFaceUpCanGiveItMinusTwoPlusTwoUntilEndOfTurn() {
        Permanent parasite = castFaceDownAndTurnFaceUp();

        harness.handlePermanentChosen(player1, parasite.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets -2/+2");

        assertThat(gqs.getEffectivePower(gd, parasite)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, parasite)).isEqualTo(5);
    }

    @Test
    void turningFaceUpCanTargetAnOpponentsCreatureAndTheBoostExpires() {
        Permanent target = addCreatureReady(player2, new ShaperParasite());
        Permanent parasite = castFaceDownAndTurnFaceUp();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, parasite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, parasite)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void toughnessReductionKillsAFaceDownCreature() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ShaperParasite()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent parasite = castFaceDownAndTurnFaceUp();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Shaper Parasite");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(parasite);
    }

    @Test
    void castingFaceUpDoesNotTriggerTheAbility() {
        harness.setHand(player1, List.of(new ShaperParasite()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shaper Parasite");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castFaceDownAndTurnFaceUp() {
        harness.setHand(player1, List.of(new ShaperParasite()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent parasite = findPermanent(player1, "Shaper Parasite");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(parasite));
        return parasite;
    }
}
