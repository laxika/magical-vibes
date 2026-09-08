package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireCovenant.class, BalduvianBears.class, IcyManipulator.class})
class FireCovenantTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new FireCovenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Pays X life and divides X damage among target creatures")
    void dividesDamageAmongCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        prepare();

        harness.castInstantForX(player1, 0, 4, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With X equal to zero it can be cast without targets")
    void zeroDamageRequiresNoTargets() {
        prepare();

        harness.castInstantForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can't be cast for more life than you have")
    void cannotPayMoreLifeThanYouHave() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        prepare();
        harness.setLife(player1, 3);

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 4, Map.of(bears.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("Players can't be assigned damage")
    void cannotTargetPlayers() {
        harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(player2.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature permanents can't be assigned damage")
    void cannotTargetNoncreatures() {
        harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(artifact.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each target must be assigned at least one damage")
    void eachTargetMustReceiveDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 4, Map.of(first.getId(), 0, second.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 3, Map.of(bears.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }
}
