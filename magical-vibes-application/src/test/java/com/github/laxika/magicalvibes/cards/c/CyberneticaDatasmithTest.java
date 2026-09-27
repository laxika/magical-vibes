package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyberneticaDatasmith.class, GrizzlyBears.class})
class CyberneticaDatasmithTest extends BaseCardTest {

    @Test
    void routesEffectsToTheirDifferentTargetPlayers() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);

        List<Permanent> player1Robots = findPermanents(player1, "Robot");
        List<Permanent> player2Robots = findPermanents(player2, "Robot");
        assertThat(player1Robots).isEmpty();
        assertThat(player2Robots).hasSize(1);
        Permanent robot = player2Robots.getFirst();
        assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, robot)).isTrue();
    }

    @Test
    void requiresDifferentPlayersAsTargets() {
        addCreatureReady(player1, new CyberneticaDatasmith());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
