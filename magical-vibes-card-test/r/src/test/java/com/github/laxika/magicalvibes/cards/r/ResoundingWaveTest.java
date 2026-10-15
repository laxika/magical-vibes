package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.d.DeftDuelist;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoundingWave.class, DregscapeZombie.class, Island.class, DeftDuelist.class})
class ResoundingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature to its owner's hand")
    void returnsTargetCreatureToHand() {
        harness.addToBattlefield(player2, new DregscapeZombie());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Dregscape Zombie");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dregscape Zombie");
        harness.assertInHand(player2, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Can return a noncreature permanent (a land)")
    void returnsTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Cycling returns two chosen permanents to owners' hands and draws a card")
    void cyclingReturnsTwoPermanentsAndDraws() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new DregscapeZombie()));
        harness.addToBattlefield(player2, new DregscapeZombie());
        harness.addToBattlefield(player2, new Island());
        addCyclingMana(player1);

        UUID zombieId = harness.getPermanentId(player2, "Dregscape Zombie");
        UUID islandId = harness.getPermanentId(player2, "Island");

        harness.activateHandAbility(player1, 0, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(zombieId, islandId));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Dregscape Zombie");
        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Dregscape Zombie");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Resounding Wave");
        harness.assertInHand(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Cycling still draws when fewer than two legal targets exist")
    void cyclingWithOnlyOneLegalTargetStillDraws() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new DregscapeZombie()));
        harness.addToBattlefield(player2, new DregscapeZombie());
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dregscape Zombie");
        harness.assertInHand(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Resounding Wave");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud cannot supply the second target for the cycling trigger")
    void cyclingDoesNotCountShroudedPermanentAsLegalTarget() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new DregscapeZombie()));
        harness.addToBattlefield(player2, new DeftDuelist());
        harness.addToBattlefield(player2, new Island());
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Deft Duelist");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Dregscape Zombie");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling with no permanents still draws a card")
    void cyclingWithNoPermanentsStillDraws() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new DregscapeZombie()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Resounding Wave");
        harness.assertNotInHand(player1, "Dregscape Zombie");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dregscape Zombie");
        assertThat(gd.stack).isEmpty();
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
