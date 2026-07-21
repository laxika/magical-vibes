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
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheScarabGodTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep: each opponent loses X life and controller scries X for X Zombies controlled")
    void upkeepLoseLifeAndScryEqualToZombies() {
        harness.addToBattlefield(player1, new TheScarabGod());
        harness.addToBattlefield(player1, new ZombieGoliath());
        harness.addToBattlefield(player1, new ZombieGoliath());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

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
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.ZOMBIE);
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
    @DisplayName("When The Scarab God dies, it returns to hand at the beginning of the next end step")
    void diesReturnsToHandAtNextEndStep() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        Card scarabCard = scarab.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves
        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scarabCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scarabCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(scarabCard.getId()));
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }
}
