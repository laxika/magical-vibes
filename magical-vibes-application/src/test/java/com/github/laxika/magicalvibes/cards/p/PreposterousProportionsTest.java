package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PreposterousProportionsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Preposterous Proportions gives your creatures +10/+10 and vigilance")
    void boostsAndGrantsVigilanceToOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(ownCreature.getEffectivePower()).isEqualTo(12);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();

        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreposterousProportions()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getEffectivePower()).isEqualTo(12);
        assertThat(creature.getEffectiveToughness()).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }
}
