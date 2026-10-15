package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.GreaterBasilisk;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasOutrage.class, RuneclawBear.class, GreaterBasilisk.class, SafePassage.class})
class ChandrasOutrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature and 2 damage to its controller")
    void deals4DamageToCreatureAnd2ToController() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Runeclaw Bear is 2/2, dies to 4 damage
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        // Controller takes 2 damage
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller even when creature survives")
    void deals2DamageToControllerWhenCreatureSurvives() {
        harness.addToBattlefield(player2, new GreaterBasilisk());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Greater Basilisk");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Greater Basilisk is 3/5, survives 4 damage (4 marked damage < 5 toughness)
        harness.assertOnBattlefield(player2, "Greater Basilisk");
        // Controller still takes 2 damage
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller even when creature dies to the 4 damage")
    void deals2DamageToControllerWhenCreatureDies() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Runeclaw Bear is 2/2, dies to 4 damage
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        // Controller must still take 2 damage even though creature was destroyed
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Chandra's Outrage");
        // No damage to controller when spell fizzles
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Chandra's Outrage");
    }

    @Test
    @DisplayName("Can target your own creature and damages you instead of the opponent")
    void damagesControllerOfOwnTarget() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Runeclaw Bear"));

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Marks exactly four damage on a surviving creature")
    void marksFourDamageOnSurvivor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterBasilisk());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Greater Basilisk");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damages the creature's current controller when control changes before resolution")
    void usesControllerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterBasilisk());
        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Safe Passage prevents both the creature damage and controller damage")
    void preventionProtectsCreatureAndController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterBasilisk());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player1, List.of(new ChandrasOutrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Greater Basilisk");
        harness.assertLife(player2, 20);
    }
}
