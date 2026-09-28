package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReconCraftTheta.class, GrizzlyBears.class})
class ReconCraftThetaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 Alien token with a +1/+1 counter")
    void enteringTheBattlefieldCreatesAlienTokenWithCounter() {
        harness.setHand(player1, List.of(new ReconCraftTheta()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent alien = findPermanents(player1, "Alien").getFirst();
        assertThat(alien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, alien)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, alien)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking after being crewed proliferates")
    void attackingAfterBeingCrewedProliferates() {
        addReadyCraft(player1);
        Permanent crew = addReadyCreature(player1);
        crew.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(crew.getId()));

        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addReadyCraft(Player player) {
        Permanent craft = new Permanent(new ReconCraftTheta());
        craft.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(craft);
        return craft;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
