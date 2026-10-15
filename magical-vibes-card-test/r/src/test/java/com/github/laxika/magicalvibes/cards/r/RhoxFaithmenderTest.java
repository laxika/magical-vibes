package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.t.TaintedRemedy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxFaithmender.class, AngelsMercy.class, TaintedRemedy.class})
class RhoxFaithmenderTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's life gain is doubled")
    void controllerLifeGainDoubled() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 26); // 3 doubled to 6
    }

    @Test
    @DisplayName("Only the controller's life gain is doubled, not the opponent's")
    void opponentLifeGainNotDoubled() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Life gained from a resolved spell is doubled")
    void spellLifeGainDoubled() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 34);
    }

    @Test
    @DisplayName("Two Faithmenders quadruple life gain")
    void multipleFaithmendersMultiplyLifeGain() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 48);
    }

    @Test
    @DisplayName("Faithmender doubles life gained from its own lifelink")
    void ownLifelinkIsDoubled() {
        addCreatureReady(player1, new RhoxFaithmender());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Increasing a life total doubles the life gained")
    void settingLifeTotalUpwardDoublesDifference() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 10);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 20));

        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Life loss is not doubled")
    void lifeLossIsNotDoubled() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "life loss"));

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The affected player chooses between Faithmender and Tainted Remedy replacements")
    void competingReplacementsRequirePlayerChoice() {
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.addToBattlefield(player2, new TaintedRemedy());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player1, 20);
    }
}
