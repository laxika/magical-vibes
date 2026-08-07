package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CruelRevivalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the non-Zombie creature and returns the targeted Zombie card to hand")
    void destroysCreatureAndReturnsZombie() {
        Card zombieInGraveyard = new DiregrafGhoul();
        harness.setGraveyard(player1, List.of(zombieInGraveyard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, zombieInGraveyard.getId(), List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(zombieInGraveyard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(zombieInGraveyard.getId()));
    }

    @Test
    @DisplayName("The graveyard target is optional — destroying alone resolves")
    void castsWithoutGraveyardTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroyed creature can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        harness.addToBattlefield(player2, new DrudgeSkeletons());
        Permanent skeletons = findPermanent(player2, "Drudge Skeletons");
        skeletons.setRegenerationShield(1);
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(skeletons.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombie() {
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID ghoulId = harness.getPermanentId(player2, "Diregraf Ghoul");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ghoulId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Zombie card in the graveyard")
    void cannotTargetNonZombieCardInGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId(), List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
