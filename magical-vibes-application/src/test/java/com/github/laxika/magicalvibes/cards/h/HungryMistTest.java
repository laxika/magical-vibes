package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HungryMist.class)
class HungryMistTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {G}{G} sacrifices Hungry Mist")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new HungryMist());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Hungry Mist");
        harness.assertInGraveyard(player1, "Hungry Mist");
    }

    @Test
    @DisplayName("Paying {G}{G} keeps Hungry Mist on the battlefield")
    void payKeeps() {
        harness.addToBattlefield(player1, new HungryMist());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Hungry Mist");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough green mana still sacrifices Hungry Mist")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new HungryMist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1); // one short
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Hungry Mist");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HungryMist());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hungry Mist");
    }

    @Test
    @DisplayName("Triggers during the controller's upkeep")
    void triggersDuringControllerUpkeep() {
        harness.addToBattlefield(player2, new HungryMist());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Hungry Mist");
        harness.assertInGraveyard(player2, "Hungry Mist");
    }

    @Test
    @DisplayName("Does not sacrifice it if another player gains control before resolution")
    void doesNotSacrificeAfterControlChanges() {
        var mist = harness.addToBattlefieldAndReturn(player1, new HungryMist());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), mist,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Hungry Mist");
        harness.assertNotInGraveyard(player1, "Hungry Mist");
    }
}
