package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakusethMawOfFlames.class, ColossalDreadmaw.class, Vorstclaw.class, Unsummon.class})
class DrakusethMawOfFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Attack deals 4 damage to the first target and 3 to each of two other targets")
    void attackDealsFourAndThreeDamage() {
        addCreatureReady(player1, new DrakusethMawOfFlames());
        Permanent firstTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent secondTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent thirdTarget = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(secondTarget.getId(), thirdTarget.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(4);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(thirdTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack can choose a player for 4 damage and decline the other targets")
    void attackCanChoosePlayerAndNoOtherTargets() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DrakusethMawOfFlames());
        addCreatureReady(player2, new ColossalDreadmaw());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Attack can choose exactly one optional target")
    void attackCanChooseOneOtherTarget() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DrakusethMawOfFlames());
        Permanent target = addCreatureReady(player2, new Vorstclaw());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        harness.assertLife(player2, 16);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing the four-damage target does not change the other targets' damage")
    void otherTargetsStillTakeThreeWhenFirstTargetLeaves() {
        addCreatureReady(player1, new DrakusethMawOfFlames());
        Permanent firstTarget = addCreatureReady(player2, new Vorstclaw());
        Permanent secondTarget = addCreatureReady(player2, new Vorstclaw());
        Permanent thirdTarget = addCreatureReady(player2, new Vorstclaw());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(secondTarget.getId(), thirdTarget.getId()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, firstTarget.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        harness.assertInHand(player2, "Vorstclaw");
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(thirdTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack trigger still deals damage after Drakuseth leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.setLife(player2, 20);
        Permanent drakuseth = addCreatureReady(player1, new DrakusethMawOfFlames());
        Permanent target = addCreatureReady(player2, new Vorstclaw());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, drakuseth.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        harness.assertInHand(player1, "Drakuseth, Maw of Flames");
        harness.assertLife(player2, 16);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }
}
