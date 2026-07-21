package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MycoidShepherdTest extends BaseCardTest {

    private void terminate(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Terminate()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
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
        harness.passBothPriorities(); // resolve Terminate → Mycoid Shepherd dies → trigger
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
        harness.passBothPriorities(); // resolve Terminate → Grizzly Bears dies

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
