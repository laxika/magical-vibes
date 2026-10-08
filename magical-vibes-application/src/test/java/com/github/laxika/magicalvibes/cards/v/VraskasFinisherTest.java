package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskasFinisher.class, GarrukWildspeaker.class, GrizzlyBears.class, ChandrasPyrohelix.class})
class VraskasFinisherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a damaged creature an opponent controls")
    void etbDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castFinisher(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB destroys a damaged planeswalker an opponent controls")
    void etbDestroysDamagedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castFinisher(target);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Cannot target an opponent creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    void cannotTargetOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VraskasFinisher());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetUndamagedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutLegalTargets() {
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vraska's Finisher");
    }

    @Test
    void destroysCreatureAfterActualSpellDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VraskasFinisher());
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, Map.of(target.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Vraska's Finisher");

        castFinisher(target);

        harness.assertNotOnBattlefield(player2, "Vraska's Finisher");
        harness.assertInGraveyard(player2, "Vraska's Finisher");
    }

    @Test
    void doesNotDestroyTargetThatChangesToOurControlBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castFinisher(Permanent target) {
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
