package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Manticore.class, GrizzlyBears.class, HillGiantHerdgorger.class, MagicMissile.class})
class ManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a damaged creature an opponent controls when it enters")
    void destroysDamagedOpponentsCreatureOnEnter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        castManticore(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a damaged creature you control")
    void cannotTargetOwnDamagedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature dealt noncombat damage by a spell this turn")
    void destroysCreatureDamagedBySpell() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new MagicMissile()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, Map.of(giant.getId(), 3));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");

        castManticore(giant);

        harness.assertNotOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Can enter when no creature is eligible for its triggered ability")
    void entersWithoutEligibleTarget() {
        harness.addToBattlefield(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Manticore");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Does not destroy a target that comes under your control before resolution")
    void doesNotDestroyTargetNowControlledByYou() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        gd.permanentsDealtDamageThisTurn.add(giant.getId());
        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, giant.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(giant);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant Herdgorger");
        harness.assertNotInGraveyard(player2, "Hill Giant Herdgorger");
    }

    private void castManticore(Permanent target) {
        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
