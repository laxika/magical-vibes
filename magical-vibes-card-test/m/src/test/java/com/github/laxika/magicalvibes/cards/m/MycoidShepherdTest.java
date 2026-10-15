package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BantSureblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfNewAlara;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MycoidShepherd.class, Terminate.class, AvatarOfMight.class, GrizzlyBears.class,
        BantSureblade.class, KnightOfNewAlara.class})
class MycoidShepherdTest extends BaseCardTest {

    private void terminate(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Terminate()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    @Test
    @DisplayName("When Mycoid Shepherd itself dies, its controller may gain 5 life")
    void selfDeathMayGainLife() {
        harness.addToBattlefield(player1, new MycoidShepherd());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        terminate(player1, harness.getPermanentId(player1, "Mycoid Shepherd"));
        harness.passBothPriorities(); // resolve trigger → MayEffect prompts

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("When another creature you control with power 5+ dies, you may gain 5 life")
    void allyPowerFiveOrGreaterDiesMayGainLife() {
        harness.addToBattlefield(player1, new MycoidShepherd());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        terminate(player1, harness.getPermanentId(player1, "Avatar of Might"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void mayDeclineGainsNoLife() {
        harness.addToBattlefield(player1, new MycoidShepherd());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        terminate(player1, harness.getPermanentId(player1, "Avatar of Might"));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A creature you control with power less than 5 does not trigger")
    void allyPowerBelowFiveDoesNotTrigger() {
        harness.addToBattlefield(player1, new MycoidShepherd());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        terminate(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    void selfDeathStillTriggersWithPowerBelowFive() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new MycoidShepherd());
        shepherd.setPowerModifier(-4);

        terminate(player1, shepherd.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 25);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherShepherdAtExactlyFivePowerTriggersBothShepherds() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new MycoidShepherd());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new MycoidShepherd());

        terminate(player1, dying.getId());
        for (int i = 0; i < 2; i++) {
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 30);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingCreatureDeathDoesNotTriggerYourShepherd() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new MycoidShepherd());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MycoidShepherd());

        terminate(player1, opposing.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 25);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousShepherdDeathsEachTriggerForBothDeaths() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MycoidShepherd());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MycoidShepherd());
        first.setMarkedDamage(4);
        second.setMarkedDamage(4);

        harness.runStateBasedActions();
        for (int i = 0; i < 4; i++) {
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 40);
        harness.assertNotOnBattlefield(player1, "Mycoid Shepherd");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dyingCreatureUsesPowerIncludingContinuousBonusesBeforeDeath() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new MycoidShepherd());
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());

        terminate(player1, sureblade.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 25);
    }
}
