package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraNalaar.class, GrizzlyBears.class, Mountain.class, Pyrotechnics.class, Spellbook.class})
class PyrotechnicsTest extends BaseCardTest {

    @Test
    void dealsAll4DamageToSingleCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(bear.getId(), 4));
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bear.getId()));
    }

    @Test
    void dividesDamageAmongTwoCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(bears1.getId(), 2, bears2.getId(), 2));
        harness.passBothPriorities();

        // Both are 2/2, both die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears1.getId()))
                .noneMatch(p -> p.getId().equals(bears2.getId()));
    }

    @Test
    void canDealAllDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void canDealAllDamageToItsController() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void canDivideDamageAmongBothPlayers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 3);
    }

    @Test
    void splitsDamageAmongCreatureAndPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 2));
        harness.passBothPriorities();

        // Bears is 2/2, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void skipsTargetThatGainsHexproofBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent protectedBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(protectedBears.getId(), 2, otherBears.getId(), 2));
        protectedBears.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(protectedBears.getMarkedDamage()).isZero();
        assertThat(otherBears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void fizzlesWhenOnlyTargetGainsHexproofBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(bear.getId(), 4));
        bear.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bear.getId()));
    }

    @Test
    void damageAssignmentsMustSumTo4() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Only assigning 2 damage — should fail
        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresAtLeastOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachTargetMustReceiveDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0,
                        Map.of(firstBear.getId(), 0, secondBear.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetPlaneswalker() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.castSorcery(player1, 0, Map.of(chandra.getId(), 4));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void cannotTargetHexproofCreatureWhenCasting() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.getGrantedKeywords().add(Keyword.HEXPROOF);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanentWhenCasting() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(mountain.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDivideOneDamageAmongFourCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent thirdBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourthBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(
                firstBear.getId(), 1,
                secondBear.getId(), 1,
                thirdBear.getId(), 1,
                fourthBear.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(
                        firstBear.getId(), secondBear.getId(), thirdBear.getId(), fourthBear.getId());
        assertThat(firstBear.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBear.getMarkedDamage()).isEqualTo(1);
        assertThat(thirdBear.getMarkedDamage()).isEqualTo(1);
        assertThat(fourthBear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(spellbook.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }
}
