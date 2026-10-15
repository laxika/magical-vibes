package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelRevival.class, BarkhideMauler.class, GluttonousZombie.class, Swamp.class})
class CruelRevivalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the non-Zombie creature and returns the targeted Zombie card to hand")
    void destroysCreatureAndReturnsZombie() {
        Card zombieInGraveyard = new GluttonousZombie();
        harness.setGraveyard(player1, List.of(zombieInGraveyard));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID maulerId = harness.getPermanentId(player2, "Barkhide Mauler");

        harness.castSorcery(player1, 0, zombieInGraveyard.getId(), List.of(maulerId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Barkhide Mauler");
        harness.assertInGraveyard(player2, "Barkhide Mauler");
        harness.assertInHand(player1, "Gluttonous Zombie");
        harness.assertNotInGraveyard(player1, "Gluttonous Zombie");

        int destroyIndex = -1;
        int returnIndex = -1;
        for (int i = 0; i < gd.gameLog.size(); i++) {
            String logEntry = gd.gameLog.get(i).plainText();
            if (logEntry.equals("Barkhide Mauler is destroyed.")) {
                destroyIndex = i;
            }
            if (logEntry.contains("returns Gluttonous Zombie from graveyard to hand.")) {
                returnIndex = i;
            }
        }
        assertThat(destroyIndex).isGreaterThanOrEqualTo(0).isLessThan(returnIndex);
    }

    @Test
    @DisplayName("The graveyard target is optional — destroying alone resolves")
    void castsWithoutGraveyardTarget() {
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID maulerId = harness.getPermanentId(player2, "Barkhide Mauler");

        harness.castAndResolveInstant(player1, 0, List.of(maulerId));

        harness.assertNotOnBattlefield(player2, "Barkhide Mauler");
    }

    @Test
    @DisplayName("The destroyed creature can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        harness.addToBattlefield(player2, new BarkhideMauler());
        Permanent mauler = findPermanent(player2, "Barkhide Mauler");
        mauler.setRegenerationShield(1);
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, List.of(mauler.getId()));

        harness.assertNotOnBattlefield(player2, "Barkhide Mauler");
        harness.assertInGraveyard(player2, "Barkhide Mauler");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombie() {
        harness.addToBattlefield(player2, new GluttonousZombie());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID zombieId = harness.getPermanentId(player2, "Gluttonous Zombie");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(zombieId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Zombie card in the graveyard")
    void cannotTargetNonZombieCardInGraveyard() {
        Card mauler = new BarkhideMauler();
        harness.setGraveyard(player1, List.of(mauler));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID battlefieldMaulerId = harness.getPermanentId(player2, "Barkhide Mauler");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mauler.getId(), List.of(battlefieldMaulerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID swampId = harness.getPermanentId(player2, "Swamp");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(swampId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a Zombie card in an opponent's graveyard")
    void cannotTargetZombieInOpponentsGraveyard() {
        Card zombie = new GluttonousZombie();
        harness.setGraveyard(player2, List.of(zombie));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID maulerId = harness.getPermanentId(player2, "Barkhide Mauler");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, zombie.getId(), List.of(maulerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can decline the Zombie target even when a Zombie is available")
    void canDeclineAvailableZombie() {
        harness.setGraveyard(player1, List.of(new GluttonousZombie()));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0,
                List.of(harness.getPermanentId(player2, "Barkhide Mauler")));

        harness.assertInGraveyard(player2, "Barkhide Mauler");
        harness.assertInGraveyard(player1, "Gluttonous Zombie");
        harness.assertNotInHand(player1, "Gluttonous Zombie");
    }

    @Test
    @DisplayName("Returns the Zombie when the creature target leaves the battlefield")
    void returnsZombieWhenCreatureTargetLeaves() {
        Card zombie = new GluttonousZombie();
        harness.setGraveyard(player1, List.of(zombie));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID maulerId = harness.getPermanentId(player2, "Barkhide Mauler");

        harness.castSorcery(player1, 0, zombie.getId(), List.of(maulerId));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(maulerId));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gluttonous Zombie");
        harness.assertNotInGraveyard(player1, "Gluttonous Zombie");
        harness.assertInGraveyard(player1, "Cruel Revival");
    }

    @Test
    @DisplayName("Destroys the creature when the Zombie target leaves the graveyard")
    void destroysCreatureWhenZombieTargetLeaves() {
        Card zombie = new GluttonousZombie();
        harness.setGraveyard(player1, List.of(zombie));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, zombie.getId(),
                List.of(harness.getPermanentId(player2, "Barkhide Mauler")));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Barkhide Mauler");
        harness.assertInGraveyard(player2, "Barkhide Mauler");
        harness.assertNotInHand(player1, "Gluttonous Zombie");
        harness.assertInGraveyard(player1, "Cruel Revival");
    }

    @Test
    @DisplayName("Cannot cast solely to return a Zombie without a creature target")
    void requiresNonZombieCreatureTarget() {
        Card zombie = new GluttonousZombie();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setHand(player1, List.of(new CruelRevival()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, zombie.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
