package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmOfRats.class, RavenousRats.class, SengirVampire.class, Diminish.class})
class SwarmOfRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Swarm of Rats is 1/1 when it is the only Rat you control")
    void isOneOneWhenOnlyRat() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats power equals the number of Rats you control; toughness stays 1")
    void powerEqualsRatsYouControl() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());
        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player1, new RavenousRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats power updates as controlled Rats enter and leave")
    void powerUpdatesAsControlledRatsChange() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);

        Permanent controlledRat = harness.addToBattlefieldAndReturn(player1, new RavenousRats());
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(controlledRat);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats counts only Rats, not other creatures you control")
    void countsOnlyRats() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());
        harness.addToBattlefield(player1, new SengirVampire());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats counts only your Rats, not opponent Rats")
    void countsOnlyControllersRats() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());
        harness.addToBattlefield(player2, new RavenousRats());
        harness.addToBattlefield(player2, new RavenousRats());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Swarms of Rats count each other")
    void multipleSwarmsCountEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SwarmOfRats());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SwarmOfRats());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Swarm of Rats in hand counts its owner's battlefield Rats without counting itself")
    void powerAbilityFunctionsInHand() {
        SwarmOfRats swarm = new SwarmOfRats();
        harness.setHand(player1, List.of(swarm));

        assertThat(gqs.getEffectiveCardPower(gd, swarm)).isZero();

        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player2, new RavenousRats());

        assertThat(gqs.getEffectiveCardPower(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swarm of Rats in the graveyard counts its owner's Rats")
    void powerAbilityFunctionsInGraveyard() {
        SwarmOfRats swarm = new SwarmOfRats();
        harness.setGraveyard(player1, List.of(swarm));
        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player2, new RavenousRats());

        assertThat(gqs.getEffectiveCardPower(gd, swarm)).isEqualTo(2);
    }

    @Test
    @CardUsed(Diminish.class)
    @DisplayName("A base power and toughness setter overrides Swarm of Rats's power ability")
    void basePowerToughnessSetterOverridesPowerAbility() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfRats());
        harness.addToBattlefield(player1, new RavenousRats());
        harness.addToBattlefield(player1, new RavenousRats());

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(1);
    }
}
