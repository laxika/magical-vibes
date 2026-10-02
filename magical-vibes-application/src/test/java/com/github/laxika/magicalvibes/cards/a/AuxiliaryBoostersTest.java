package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuxiliaryBoosters.class, GrizzlyBears.class})
class AuxiliaryBoostersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates and attaches a Robot artifact creature")
    void enteringCreatesAndAttachesRobot() {
        harness.setHand(player1, List.of(new AuxiliaryBoosters()));
        addManaForAuxiliaryBoosters();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent boosters = findPermanent(player1, "Auxiliary Boosters");
        Permanent robot = findPermanent(player1, "Robot");

        assertThat(boosters.getAttachedTo()).isEqualTo(robot.getId());
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, robot, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} grants +1/+2 and flying to the equipped creature")
    void equipGrantsBoostAndFlying() {
        Permanent boosters = harness.addToBattlefieldAndReturn(player1, new AuxiliaryBoosters());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boosters.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Robot is still created if the Equipment leaves before its trigger resolves")
    void createsRobotWithoutEquipment() {
        harness.setHand(player1, List.of(new AuxiliaryBoosters()));
        addManaForAuxiliaryBoosters();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent boosters = findPermanent(player1, "Auxiliary Boosters");
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(boosters);
        gd.playerGraveyards.get(player1.getId()).add(boosters.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Robot")).isEqualTo(1);
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, robot, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each Equipment attaches to its own Robot, and re-equipping removes its old bonuses")
    void multipleBoostersAndReequip() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new AuxiliaryBoosters());
        resolveAllTriggers();
        Permanent firstRobot = findPermanent(player1, "Robot");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new AuxiliaryBoosters());
        resolveAllTriggers();
        Permanent secondRobot = findPermanents(player1, "Robot").get(1);

        assertThat(countPermanents(player1, "Robot")).isEqualTo(2);
        assertThat(first.getAttachedTo()).isEqualTo(firstRobot.getId());
        assertThat(second.getAttachedTo()).isEqualTo(secondRobot.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, secondRobot.getId());
        harness.passBothPriorities();

        assertThat(first.getAttachedTo()).isEqualTo(secondRobot.getId());
        assertThat(gqs.getEffectivePower(gd, firstRobot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstRobot)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstRobot, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondRobot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondRobot)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, secondRobot, Keyword.FLYING)).isTrue();
    }

    private void addManaForAuxiliaryBoosters() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
