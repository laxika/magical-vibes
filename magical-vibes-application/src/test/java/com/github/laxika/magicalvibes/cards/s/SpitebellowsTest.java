package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spitebellows.class, BurrentonBombardier.class})
class SpitebellowsTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: Spitebellows enters normally and stays on the battlefield")
    void hardcastStaysOnBattlefield() {
        harness.setHand(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spitebellows");
        harness.assertNotInGraveyard(player1, "Spitebellows");
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB deals 6 damage to target creature and kills it")
    void evokeSacrificesAndDealsDamage() {
        Permanent target = addCreatureReady(player2, new BurrentonBombardier()); // 2/2
        harness.setHand(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers(); // resolve creature and ETB (evoke sacrifice) -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB damage

        harness.assertNotOnBattlefield(player2, "Burrenton Bombardier");
        // Spitebellows itself was sacrificed as it entered.
        harness.assertNotOnBattlefield(player1, "Spitebellows");
    }

    @Test
    @DisplayName("LTB fires on any leave: deals 6 damage to target creature, killing a 2/2")
    void destroyedDealsDamageAndKills() {
        Permanent target = addCreatureReady(player2, new BurrentonBombardier()); // 2/2
        Permanent spitebellows = harness.addToBattlefieldAndReturn(player1, new Spitebellows());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spitebellows));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB damage

        harness.assertNotOnBattlefield(player2, "Burrenton Bombardier");
    }

    @Test
    @DisplayName("LTB deals exactly 6 damage: a tougher creature survives with 6 marked damage")
    void tougherTargetSurvives() {
        BurrentonBombardier bear = new BurrentonBombardier();
        bear.setToughness(7);
        Permanent target = addCreatureReady(player2, bear);
        Permanent spitebellows = harness.addToBattlefieldAndReturn(player1, new Spitebellows());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spitebellows));

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

    @Test
    @DisplayName("LTB can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new BurrentonBombardier());
        Permanent spitebellows = harness.addToBattlefieldAndReturn(player1, new Spitebellows());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spitebellows));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Burrenton Bombardier");
    }
}
