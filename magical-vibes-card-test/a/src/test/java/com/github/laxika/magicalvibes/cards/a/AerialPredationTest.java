package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KeeningApparition;
import com.github.laxika.magicalvibes.cards.r.RootbornDefenses;
import com.github.laxika.magicalvibes.cards.t.TowerDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialPredation.class, TowerDrake.class, KeeningApparition.class, RootbornDefenses.class})
class AerialPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted flier and the caster gains 2 life")
    void destroysFlierAndGainsLife() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new TowerDrake());

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tower Drake");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new TowerDrake());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new KeeningApparition());

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new TowerDrake());

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Aerial Predation");
    }

    @Test
    @DisplayName("Can destroy your own flying creature and still gain life")
    void canTargetOwnFlier() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TowerDrake());
        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, drake.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tower Drake");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains life even when indestructible prevents destruction")
    void gainsLifeWhenTargetIsIndestructible() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new TowerDrake());
        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, drake.getId());

        harness.setHand(player2, List.of(new RootbornDefenses()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tower Drake");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Aerial Predation");
    }
}
