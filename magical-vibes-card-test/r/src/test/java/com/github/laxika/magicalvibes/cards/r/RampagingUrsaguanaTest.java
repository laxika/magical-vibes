package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingUrsaguana.class, Shock.class})
class RampagingUrsaguanaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 for each time it attacked this game")
    void boostsForEachAttackThisGame() {
        Permanent ursaguana = addCreatureReady(player1, new RampagingUrsaguana());

        assertThat(gqs.getEffectivePower(gd, ursaguana)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ursaguana)).isEqualTo(4);

        declareAttackers(List.of(0));

        assertThat(gqs.getEffectivePower(gd, ursaguana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursaguana)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ursaguana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursaguana)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward protects the face-up creature")
    void wardProtectsFaceUpCreature() {
        addCreatureReady(player1, new RampagingUrsaguana());
        Permanent ursaguana = findPermanent(player1, "Rampaging Ursaguana");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, ursaguana.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ursaguana);
    }

    @Test
    @DisplayName("Disguise grants ward while face down")
    void disguiseGrantsWardFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RampagingUrsaguana()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ursaguana = findPermanent(player1, "Rampaging Ursaguana");
        assertThat(ursaguana.isFaceDown()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, ursaguana.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rampaging Ursaguana").isFaceDown()).isTrue();
    }
}
