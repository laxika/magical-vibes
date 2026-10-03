package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlisteningSphere.class, GrizzlyBears.class})
class GlisteningSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and proliferates")
    void entersTappedAndProliferates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GlisteningSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent sphere = findPermanent(player1, "Glistening Sphere");
        assertThat(sphere.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Basic mana ability adds one mana of the chosen color")
    void addsOneManaOfAnyColor() {
        Permanent sphere = addReadySphere();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(sphere.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Corrupted mana ability adds three mana when an opponent has three poison counters")
    void addsThreeManaWithCorrupted() {
        Permanent sphere = addReadySphere();
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(sphere.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Corrupted mana ability requires three poison counters on an opponent")
    void corruptedAbilityRequiresPoisonCounters() {
        addReadySphere();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent must have at least 3 poison counters");
    }

    private Permanent addReadySphere() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new GlisteningSphere());
        sphere.setSummoningSick(false);
        return sphere;
    }
}
