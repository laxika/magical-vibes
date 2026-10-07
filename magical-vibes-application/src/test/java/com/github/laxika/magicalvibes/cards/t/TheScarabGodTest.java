package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheScarabGod.class, GrizzlyBears.class, Plains.class, WrathOfGod.class, ZombieGoliath.class})
class TheScarabGodTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep: each opponent loses X life and controller scries X for X Zombies controlled")
    void upkeepLoseLifeAndScryEqualToZombies() {
        harness.addToBattlefield(player1, new TheScarabGod());
        harness.addToBattlefield(player1, new ZombieGoliath());
        harness.addToBattlefield(player1, new ZombieGoliath());

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve: lose life, then scry

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // X=2 zombies
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Upkeep with zero Zombies: no life loss and no scry")
    void upkeepWithZeroZombiesDoesNothing() {
        harness.addToBattlefield(player1, new TheScarabGod());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve with X=0

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Activated ability exiles a creature from any graveyard and creates a 4/4 black Zombie copy")
    void activatedAbilityCreatesFourFourBlackZombieCopy() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(scarab);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getId().equals(bears.getId()));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Activated ability rejects non-creature graveyard targets")
    void activatedAbilityRejectsNonCreature() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        Card land = new Plains();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(scarab);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, idx, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep counts Zombies at resolution, including a token created in response")
    void upkeepCountsZombiesAtResolution() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        harness.addToBattlefield(player2, new ZombieGoliath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scarab), 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheScarabGod());
        harness.addToBattlefield(player1, new ZombieGoliath());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Two activations targeting the same card create only one token")
    void removedGraveyardTargetDoesNotCreateAnotherToken() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(scarab);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When The Scarab God dies, it returns to hand at the beginning of the next end step")
    void diesReturnsToHandAtNextEndStep() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        Card scarabCard = scarab.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves
        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scarabCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(scarabCard.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scarabCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(scarabCard.getId()));
    }
}
