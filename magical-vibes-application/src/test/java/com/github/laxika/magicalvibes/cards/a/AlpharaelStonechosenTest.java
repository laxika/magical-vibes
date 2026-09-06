package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlpharaelStonechosen.class, GrizzlyBears.class, Shock.class, StarfieldShepherd.class, Swamp.class})
class AlpharaelStonechosenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without a Void event only deals combat damage")
    void attackingWithoutVoidEventOnlyDealsCombatDamage() {
        addReadyAlpharael();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Void triggers when a nonland permanent left the battlefield")
    void voidTriggersAfterNonlandPermanentLeavesBattlefield() {
        addReadyAlpharael();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.setLife(player2, 19);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 6);
    }

    @Test
    @DisplayName("Void does not trigger when only a land left the battlefield")
    void voidDoesNotTriggerAfterLandLeavesBattlefield() {
        addReadyAlpharael();
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, swamp));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Void triggers after a spell was cast for its Warp cost")
    void voidTriggersAfterWarpSpell() {
        addReadyAlpharael();
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player2, List.of(shepherd));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreatureWithAlternateCost(player2, 0, List.of());
        harness.passBothPriorities();
        harness.setLife(player2, 19);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 6);
    }

    @Test
    @DisplayName("Ward discards a random card without opening a card-choice interaction")
    void wardDiscardsRandomCard() {
        Permanent alpharael = addReadyAlpharael();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(shock, bears));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, alpharael.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the targeted spell when its controller has no card")
    void wardCountersWhenHandIsEmptyAfterCasting() {
        Permanent alpharael = addReadyAlpharael();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, alpharael.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(alpharael.getMarkedDamage()).isZero();
    }

    private Permanent addReadyAlpharael() {
        Permanent alpharael = new Permanent(new AlpharaelStonechosen());
        alpharael.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(alpharael);
        return alpharael;
    }
}
