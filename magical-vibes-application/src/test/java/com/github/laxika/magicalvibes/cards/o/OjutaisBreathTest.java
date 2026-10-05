package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AncientCarp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjutaisBreath.class, AncientCarp.class, Forest.class})
class OjutaisBreathTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureAndSkipsItsNextUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Ojutai's Breath");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void restrictionExpiresAfterOnlyTheCreaturesControllersNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new OjutaisBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void decliningReboundLeavesTheCardInExileWithoutAnotherOffer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void losingTheTargetPreventsResolutionAndRebound() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ojutai's Breath");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OjutaisBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
