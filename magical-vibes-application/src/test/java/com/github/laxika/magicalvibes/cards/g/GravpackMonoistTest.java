package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravpackMonoist.class, Shock.class})
class GravpackMonoistTest extends BaseCardTest {

    @Test
    void createsTappedRobotTokenWhenItDies() {
        harness.addToBattlefield(player1, new GravpackMonoist());

        killWithShock(player2, player1, "Gravpack Monoist");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gravpack Monoist"));

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(1);
        Permanent robot = robots.getFirst();
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(robot.getCard().getColors()).isEmpty();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
