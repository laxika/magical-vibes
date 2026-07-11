package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpitebellowsTest extends BaseCardTest {

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB deals 6 damage to target creature and kills it")
    void evokeSacrificesAndDealsDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB damage

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Spitebellows itself was sacrificed as it entered.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spitebellows"));
    }

    // ===== Leaves the battlefield (any means) =====

    @Test
    @DisplayName("LTB fires on any leave: deals 6 damage to target creature, killing a 2/2")
    void destroyedDealsDamageAndKills() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears()); // 2/2
        Permanent spitebellows = harness.addToBattlefieldAndReturn(player1, new Spitebellows());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spitebellows);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB damage

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("LTB deals exactly 6 damage: a tougher creature survives with 6 marked damage")
    void tougherTargetSurvives() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(7);
        Permanent target = addCreatureReady(player2, bear);
        Permanent spitebellows = harness.addToBattlefieldAndReturn(player1, new Spitebellows());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spitebellows);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB damage

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }
}
