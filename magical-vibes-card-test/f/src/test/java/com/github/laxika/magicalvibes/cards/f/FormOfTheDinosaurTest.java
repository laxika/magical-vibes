package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.cards.s.SadisticSkymarcher;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FormOfTheDinosaur.class, HardyVeteran.class, SadisticSkymarcher.class, ZetalpaPrimalDawn.class})
class FormOfTheDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield sets its controller's life total to 15")
    void enteringSetsLifeTotalToFifteen() {
        harness.setLife(player1, 4);
        harness.castFromHand(player1, new FormOfTheDinosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Upkeep trigger damages the target creature and the enchantment's controller simultaneously")
    void upkeepDamagesTargetAndController() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HardyVeteran());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.findPermanentById(gd, target.getId())).isNull();
    }

    @Test
    @DisplayName("Upkeep trigger only offers creatures controlled by an opponent")
    void upkeepTargetIsOpponentCreature() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HardyVeteran());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("A target that leaves before resolution deals no damage to the controller")
    void targetLeavingBeforeResolutionDealsNoDamage() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HardyVeteran());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void enteringLowersLifeTotalAndDoesNotChangeOpponentsLife() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 24);
        harness.castFromHand(player1, new FormOfTheDinosaur(), "{4}{R}{R}");

        harness.passBothPriorities();
        harness.assertLife(player1, 30);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 24);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        harness.addToBattlefield(player2, new HardyVeteran());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Hardy Veteran");
    }

    @Test
    void upkeepWithNoOpponentCreaturesDoesNotDealDamage() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        harness.addToBattlefield(player1, new HardyVeteran());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void targetCreatureIsTheDamageSourceForLifelink() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SadisticSkymarcher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player2, "Sadistic Skymarcher");
    }

    @Test
    void indestructibleTargetSurvivesWithFifteenDamageAndDealsItsPower() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertOnBattlefield(player2, "Zetalpa, Primal Dawn");
        assertThat(target.getMarkedDamage()).isEqualTo(15);
    }

    @Test
    @CardUsed(EverlastingTorment.class)
    void simultaneousDamageUsesPowerBeforeWitherCountersArePlaced() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        harness.addToBattlefield(player1, new EverlastingTorment());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HardyVeteran());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Hardy Veteran");
    }
}
