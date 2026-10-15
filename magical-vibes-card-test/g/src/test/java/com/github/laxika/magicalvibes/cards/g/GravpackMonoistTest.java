package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PlasmaBolt;
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

@CardUsed({GravpackMonoist.class, PlasmaBolt.class, Gravkill.class})
class GravpackMonoistTest extends BaseCardTest {

    @Test
    void createsTappedRobotTokenWhenItDies() {
        harness.addToBattlefield(player1, new GravpackMonoist());

        killWithPlasmaBolt(player2, player1, "Gravpack Monoist");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gravpack Monoist");

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(1);
        Permanent robot = robots.getFirst();
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(robot.getCard().getColors()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        assertThat(robot.isAttacking()).isFalse();
        assertThat(findPermanents(player2, "Robot")).isEmpty();
    }

    @Test
    void createsTokenForTheOtherPlayersCreatureWhenItDies() {
        harness.addToBattlefield(player2, new GravpackMonoist());

        killWithPlasmaBolt(player1, player2, "Gravpack Monoist");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
        assertThat(findPermanents(player2, "Robot")).hasSize(1);
        assertThat(findPermanent(player2, "Robot").isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Gravpack Monoist");
    }

    @Test
    void doesNotCreateTokenWhenExiled() {
        harness.addToBattlefield(player1, new GravpackMonoist());
        harness.setHand(player2, List.of(new Gravkill()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Gravpack Monoist"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gravpack Monoist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gravpack Monoist"));
        harness.assertNotInGraveyard(player1, "Gravpack Monoist");
        assertThat(findPermanents(player1, "Robot")).isEmpty();
        assertThat(findPermanents(player2, "Robot")).isEmpty();
    }

    private void killWithPlasmaBolt(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new PlasmaBolt()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveSorcery(caster, 0, 0, targetId);
    }
}
