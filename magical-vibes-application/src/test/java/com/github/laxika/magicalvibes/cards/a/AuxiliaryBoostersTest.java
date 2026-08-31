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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent boosters = new Permanent(new AuxiliaryBoosters());
        boosters.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(boosters);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boosters.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    private void addManaForAuxiliaryBoosters() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
