package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.s.ShepherdOfTheLost;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaloHunter.class, ShepherdOfTheLost.class,
        GiantScorpion.class, StoneworkPuma.class})
class HaloHunterTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetAngel() {
        harness.addToBattlefield(player2, new ShepherdOfTheLost());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Shepherd of the Lost");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Shepherd of the Lost");
        harness.assertInGraveyard(player2, "Shepherd of the Lost");
    }

    @Test
    void cannotTargetNonAngel() {
        harness.addToBattlefield(player2, new GiantScorpion());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");

        harness.addToBattlefield(player2, new ShepherdOfTheLost());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerWhenNoAngelExists() {
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Halo Hunter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mustDestroyAnAngelControlledByItsController() {
        harness.addToBattlefield(player1, new ShepherdOfTheLost());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Shepherd of the Lost"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Halo Hunter");
        harness.assertNotOnBattlefield(player1, "Shepherd of the Lost");
        harness.assertInGraveyard(player1, "Shepherd of the Lost");
    }

    @Test
    void destroysOnlyTheChosenAngel() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ShepherdOfTheLost());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ShepherdOfTheLost());
        harness.setHand(player1, List.of(new HaloHunter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other).doesNotContain(chosen);
        harness.assertInGraveyard(player2, "Shepherd of the Lost");
        harness.assertOnBattlefield(player1, "Halo Hunter");
    }

    @Test
    void intimidateRestrictsBlockersToArtifactsOrSharedColors() {
        Permanent attacker = addCreatureReady(player1, new HaloHunter());
        Permanent whiteBlocker = addCreatureReady(player2, new ShepherdOfTheLost());
        Permanent blackBlocker = addCreatureReady(player2, new GiantScorpion());
        Permanent artifactBlocker = addCreatureReady(player2, new StoneworkPuma());

        assertThat(bls.canBlockAttacker(gd, whiteBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
