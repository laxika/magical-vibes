package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.MouseTrapper;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShortBow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouseTrapper.class, GiantGrowth.class, GrizzlyBears.class, ShortBow.class})
class MouseTrapperTest extends BaseCardTest {

    @Test
    void valiantTapsAnOpponentsCreatureWhenTargetedByYourSpell() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void valiantTriggersOnlyOnceEachTurn() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.handlePermanentChosen(player1, firstOpponentCreature.getId());
        harness.passBothPriorities();
        assertThat(firstOpponentCreature.isTapped()).isTrue();

        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(secondOpponentCreature.isTapped()).isFalse();
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, mouseTrapper.getId());

        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void valiantTriggersForEquipBeforeTheEquipmentAttaches() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ShortBow());
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MouseTrapper());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, mouseTrapper.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(bow.getAttachedTo()).isNull();

        harness.passBothPriorities();
        assertThat(bow.getAttachedTo()).isEqualTo(mouseTrapper.getId());
    }

    @Test
    void anOpponentsSpellDoesNotConsumeTheFirstFriendlyTargetingEvent() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MouseTrapper());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, mouseTrapper.getId());
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void valiantCanTriggerAgainDuringTheOpponentsTurn() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MouseTrapper());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(opponentCreature.isTapped()).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, mouseTrapper.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void valiantCannotTapACreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new ShortBow());
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MouseTrapper());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, mouseTrapper.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ally.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ally.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void flashAllowsCastingDuringTheOpponentsMainPhase() {
        harness.setHand(player1, List.of(new MouseTrapper()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mouse Trapper");
        harness.assertNotInHand(player1, "Mouse Trapper");
    }
}
