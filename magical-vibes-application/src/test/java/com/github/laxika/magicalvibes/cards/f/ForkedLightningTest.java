package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForkedLightningTest extends BaseCardTest {

    @Test
    void deals4DamageToSingleCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(target.getId(), 4));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // AirElemental is 4/4, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
    }

    @Test
    void divides2And2DamageAmongTwoCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, Map.of(target1.getId(), 2, target2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both are 2/2, both die to 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target1.getId()))
                .noneMatch(p -> p.getId().equals(target2.getId()));
    }

    @Test
    void divides2And1And1DamageAmongThreeCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        harness.castInstant(player1, 0, Map.of(
                bears.getId(), 2,
                elves.getId(), 1,
                wizard.getId(), 1
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears 2/2 dies to 2, the two 1/1s die to 1 each
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()))
                .noneMatch(p -> p.getId().equals(elves.getId()))
                .noneMatch(p -> p.getId().equals(wizard.getId()));
    }

    @Test
    void cannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo4() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        // Only assigning 3 damage — should fail
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }
}
