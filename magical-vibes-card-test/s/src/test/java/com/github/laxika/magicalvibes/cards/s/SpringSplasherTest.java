package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringSplasher.class, GrizzlyBears.class})
class SpringSplasherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives a chosen defending creature -3/-0 until end of turn")
    void attackTriggerWeakensDefendingCreature() {
        addCreatureReady(player1, new SpringSplasher());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isZero();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only a creature the defending player controls is a legal target")
    void onlyDefendingCreaturesAreLegalTargets() {
        addCreatureReady(player1, new SpringSplasher());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("The attack trigger is skipped when the defending player controls no creatures")
    void noTargetSkipsTrigger() {
        addCreatureReady(player1, new SpringSplasher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @CardUsed(SpringSplasher.class)
    @DisplayName("The attack trigger still resolves after Spring Splasher leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new SpringSplasher());
        Permanent target = addCreatureReady(player2, new SpringSplasher());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(SpringSplasher.class)
    @DisplayName("A target that leaves the battlefield is not replaced by another creature")
    void missingTargetDoesNotWeakenAnotherCreature() {
        addCreatureReady(player1, new SpringSplasher());
        Permanent target = addCreatureReady(player2, new SpringSplasher());
        Permanent other = addCreatureReady(player2, new SpringSplasher());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(SpringSplasher.class)
    @DisplayName("A target that changes to the attacking player's control becomes illegal")
    void targetMustStillBelongToDefendingPlayerAtResolution() {
        addCreatureReady(player1, new SpringSplasher());
        Permanent target = addCreatureReady(player2, new SpringSplasher());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
