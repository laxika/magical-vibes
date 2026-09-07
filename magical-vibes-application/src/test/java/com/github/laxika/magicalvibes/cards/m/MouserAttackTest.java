package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouserAttack.class, GrizzlyBears.class, Spellbook.class})
class MouserAttackTest extends BaseCardTest {

    @Test
    void firstModeCreatesColorlessRobotArtifactCreatureToken() {
        harness.setHand(player1, List.of(new MouserAttack()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(robot.getCard().getAdditionalTypes()).containsExactly(CardType.ARTIFACT);
        assertThat(robot.getCard().getColors()).isEmpty();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
    }

    @Test
    void secondModeBoostsTargetCreatureAndGrantsFirstStrikeUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();
        harness.setHand(player1, List.of(new MouserAttack()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void secondModeRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new MouserAttack()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .hasMessageContaining("creature");
    }
}
