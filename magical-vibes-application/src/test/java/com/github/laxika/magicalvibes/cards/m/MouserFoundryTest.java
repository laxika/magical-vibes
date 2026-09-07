package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouserFoundry.class, AirElemental.class, Spellbook.class})
class MouserFoundryTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesRobotToken() {
        harness.setHand(player1, List.of(new MouserFoundry()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
    }

    @Test
    void sacrificingItselfDealsDamageAndCreatesRobotWhenItLeaves() {
        harness.addToBattlefield(player1, new MouserFoundry());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mouser Foundry");
        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void sacrificeAbilityRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new MouserFoundry());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spellbook.getId()))
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Mouser Foundry");
    }
}
