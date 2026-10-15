package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForkedLightning.class, GrizzlyBears.class, RagingGoblin.class, Mountain.class})
class ForkedLightningTest extends BaseCardTest {

    @Test
    void deals4DamageToSingleCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(target.getId(), 4));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void divides2And2DamageAmongTwoCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(target1.getId(), 2, target2.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void divides2And1And1DamageAmongThreeCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent goblin1 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent goblin2 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 2,
                goblin1.getId(), 1,
                goblin2.getId(), 1
        ));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    void cannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo4() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Only assigning 3 damage â€” should fail
        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(target.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetMoreThanThreeCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target1 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent target3 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent target4 = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(
                        target1.getId(), 1,
                        target2.getId(), 1,
                        target3.getId(), 1,
                        target4.getId(), 1))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachTargetMustReceiveAtLeastOneDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(first.getId(), 4, second.getId(), 0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(target.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageToTargetThatGainsHexproofBeforeResolutionIsNotDealt() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(
                protectedTarget.getId(), 2,
                legalTarget.getId(), 2));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(protectedTarget.getId()))
                .noneMatch(p -> p.getId().equals(legalTarget.getId()));
    }
    @Test
    void cannotCastWithoutTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageFromAnIllegalTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(protectedTarget.getId(), 3, legalTarget.getId(), 1));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(legalTarget.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forked Lightning");
    }

    @Test
    void dealsNoDamageWhenEveryTargetBecomesIllegal() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedLightning()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(target.getId(), 4));
        target.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Forked Lightning");
    }
}
