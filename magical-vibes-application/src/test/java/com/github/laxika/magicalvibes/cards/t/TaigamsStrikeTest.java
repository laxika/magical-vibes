package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaigamsStrike.class, GrizzlyBears.class})
class TaigamsStrikeTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureAndMakesItUnblockable() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TaigamsStrike card = new TaigamsStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(bear.isCantBeBlocked()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TaigamsStrike()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TaigamsStrike card = new TaigamsStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(bear.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Taigam's Strike");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new TaigamsStrike()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
