package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuperMutantScavenger;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BottleCapBlast.class, GrizzlyBears.class, WurmsTooth.class, SuperMutantScavenger.class, ChoMannoRevolutionary.class})
class BottleCapBlastTest extends BaseCardTest {

    @Test
    @DisplayName("deals 5 damage and creates tapped Treasures for excess damage")
    void createsTappedTreasuresForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBottleCapBlast(target.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(3).allMatch(Permanent::isTapped);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("does not create Treasures without excess damage")
    void doesNotCreateTreasuresWithoutExcessDamage() {
        Card targetCard = new GrizzlyBears();
        targetCard.setToughness(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        castBottleCapBlast(target.getId());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("does not create Treasures when targeting a player")
    void doesNotCreateTreasuresWhenTargetingPlayer() {
        harness.setLife(player2, 20);

        castBottleCapBlast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Improvise can pay the generic mana with an artifact")
    void improvisePaysGenericMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BottleCapBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Exact lethal damage creates no Treasures")
    void exactLethalDamageCreatesNoTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuperMutantScavenger());

        castBottleCapBlast(target.getId());

        harness.assertNotOnBattlefield(player2, "Super Mutant Scavenger");
        harness.assertInGraveyard(player2, "Super Mutant Scavenger");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Previously marked damage increases excess damage")
    void previouslyMarkedDamageIncreasesExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setMarkedDamage(1);

        castBottleCapBlast(target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(4).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Prevented damage creates no Treasures")
    void preventedDamageCreatesNoTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());

        castBottleCapBlast(target.getId());

        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An illegal target at resolution creates no Treasures")
    void illegalTargetAtResolutionCreatesNoTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BottleCapBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bottle-Cap Blast");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Excess damage to your own permanent also creates Treasures")
    void excessDamageToOwnPermanentCreatesTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBottleCapBlast(target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(3).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Improvise cannot pay the red mana requirement")
    void improviseCannotPayColoredMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BottleCapBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Bottle-Cap Blast");
    }

    private void castBottleCapBlast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BottleCapBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
