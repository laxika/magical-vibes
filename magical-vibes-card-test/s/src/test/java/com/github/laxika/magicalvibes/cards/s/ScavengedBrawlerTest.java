package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengedBrawler.class, GrizzlyBears.class, Mountain.class})
class ScavengedBrawlerTest extends BaseCardTest {

    private void readyAbility() {
        harness.setGraveyard(player1, List.of(new ScavengedBrawler()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Ability puts four +1/+1 counters and four keyword counters on target creature")
    void abilityPutsCountersOnTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Exiling the card is part of the activation cost")
    void exilesSourceAsCost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Scavenged Brawler");
    }

    @Test
    @DisplayName("Ability requires a creature target")
    void abilityRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyAbility();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ScavengedBrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
