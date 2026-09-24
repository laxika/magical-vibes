package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SyphonSliver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TauntingSliver.class, SyphonSliver.class, GrizzlyBears.class})
class TauntingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver entering under your control goads an opponent's creature")
    void sliverEnteringGoadsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new TauntingSliver());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted trigger targets only creatures an opponent controls")
    void grantedTriggerTargetsOnlyOpponentsCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TauntingSliver());

        harness.enterBattlefieldAndReturn(player1, new SyphonSliver());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Slivers do not gain Taunting Sliver's enter trigger")
    void nonSliversDoNotGainEnterTrigger() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TauntingSliver());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }
}
