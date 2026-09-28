package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DodgyJalopy.class, AirElemental.class, GrizzlyBears.class, Mountain.class})
class DodgyJalopyTest extends BaseCardTest {

    @Test
    void powerIsGreatestManaValueAmongYourCreatures() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, jalopy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jalopy)).isEqualTo(5);
    }

    @Test
    void crewTurnsJalopyIntoACreature() {
        Permanent jalopy = harness.addToBattlefieldAndReturn(player1, new DodgyJalopy());
        Permanent crewer = addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jalopy)).isTrue();
        assertThat(crewer.isTapped()).isTrue();
    }

    @Test
    void scavengeUsesTheJalopysDynamicPower() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Dodgy Jalopy");
    }

    @Test
    void scavengeRequiresACreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new DodgyJalopy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
