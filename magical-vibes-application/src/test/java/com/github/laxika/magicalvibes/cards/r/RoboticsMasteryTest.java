package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FlashThompsonSpiderFan;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoboticsMastery.class, FlashThompsonSpiderFan.class})
class RoboticsMasteryTest extends BaseCardTest {

    @Test
    void entersAttachedBoostsCreatureAndCreatesFlyingRobotArtifacts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FlashThompsonSpiderFan());
        harness.setHand(player1, List.of(new RoboticsMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        List<Permanent> robots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(robots).hasSize(2);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
            assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, robot, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void canBeCastDuringOpponentsUpkeepAndTokensBelongToAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FlashThompsonSpiderFan());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new RoboticsMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(countPermanents(player1, "Robot")).isEqualTo(2);
        assertThat(countPermanents(player2, "Robot")).isZero();
        assertThat(findPermanents(player1, "Robot")).allSatisfy(robot -> {
            assertThat(robot.getCard().getColors()).isEmpty();
            assertThat(robot.isTapped()).isFalse();
            assertThat(robot.isAttacking()).isFalse();
        });
        assertThat(findPermanent(player1, "Robotics Mastery").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void disappearingTargetPreventsAuraFromEnteringAndCreatingTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FlashThompsonSpiderFan());
        harness.setHand(player1, List.of(new RoboticsMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Robotics Mastery");
        harness.assertNotOnBattlefield(player1, "Robotics Mastery");
        assertThat(countPermanents(player1, "Robot")).isZero();
    }

    @Test
    void tokenTriggerStillResolvesAfterAuraLeavesAndBoostEndsImmediately() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FlashThompsonSpiderFan());
        harness.setHand(player1, List.of(new RoboticsMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Robotics Mastery");
        assertThat(countPermanents(player1, "Robot")).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Robot")).isEqualTo(2);
    }
}
