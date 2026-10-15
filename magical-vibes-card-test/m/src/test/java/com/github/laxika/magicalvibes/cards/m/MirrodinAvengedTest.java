package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlabasterHostIntercessor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrodinAvenged.class, GrizzlyBears.class, Forest.class,
        AlabasterHostIntercessor.class, VolcanicSpite.class})
class MirrodinAvengedTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature that was dealt damage this turn and draws a card")
    void destroysDamagedCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysCreatureAfterActualNoncombatDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlabasterHostIntercessor());
        harness.setHand(player1, List.of(new VolcanicSpite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Alabaster Host Intercessor");

        harness.setLibrary(player1, List.of(new VolcanicSpite()));
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alabaster Host Intercessor");
        harness.assertNotOnBattlefield(player2, "Alabaster Host Intercessor");
        harness.assertInHand(player1, "Volcanic Spite");
    }

    @Test
    void doesNotDrawWhenOnlyTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlabasterHostIntercessor());
        gd.recordDamageToPermanent(creature.getId(), 1);
        harness.setLibrary(player1, List.of(new VolcanicSpite()));
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Volcanic Spite");
        harness.assertInGraveyard(player1, "Mirrodin Avenged");
    }

    @Test
    void drawsEvenWhenDamagedCreatureIsIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlabasterHostIntercessor());
        creature.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        gd.recordDamageToPermanent(creature.getId(), 1);
        harness.setLibrary(player1, List.of(new VolcanicSpite()));
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alabaster Host Intercessor");
        harness.assertNotInGraveyard(player2, "Alabaster Host Intercessor");
        harness.assertInHand(player1, "Volcanic Spite");
    }

    @Test
    void cannotTargetNoncreatureEvenIfDamageHistoryContainsIt() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.recordDamageToPermanent(land.getId(), 1);
        harness.setHand(player1, List.of(new MirrodinAvenged()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
