package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Shocker.class, FightingDrake.class, Mountain.class, SoulsFire.class, LightningBlast.class})
class ShockerTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player discards their hand and draws that many cards")
    void damagedPlayerWheelsTheirHand() {
        addAttackingShocker(player1);
        harness.setHand(player2, List.of(new FightingDrake(), new FightingDrake(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty-handed damaged player draws nothing")
    void emptyHandDrawsNothing() {
        addAttackingShocker(player1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The attacking player's own hand is untouched")
    void controllerHandUntouched() {
        addAttackingShocker(player1);
        harness.setHand(player1, List.of(new FightingDrake(), new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when it deals no combat damage to a player")
    void blockedShockerDoesNotTrigger() {
        addAttackingShocker(player1);
        harness.setHand(player2, List.of(new FightingDrake(), new Mountain()));

        Permanent blocker = addCreatureReady(player2, new FightingDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage to a player also triggers the ability")
    void noncombatDamageTriggersAbility() {
        Permanent shocker = addCreatureReady(player1, new Shocker());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new FightingDrake(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        harness.castInstant(player1, 0, List.of(shocker.getId(), player2.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger uses the hand at resolution and survives Shocker leaving the battlefield")
    void responseChangesHandBeforeTriggerResolves() {
        addAttackingShocker(player1);
        Permanent shocker = findPermanent(player1, "Shocker");
        FightingDrake discardedCard = new FightingDrake();
        Mountain drawnCard = new Mountain();
        harness.setHand(player2, List.of(new LightningBlast(), discardedCard));
        harness.setLibrary(player2, List.of(drawnCard, new Mountain()));
        harness.addMana(player2, ManaColor.RED, 4);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, shocker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Shocker");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discardedCard);
        harness.assertInGraveyard(player2, "Lightning Blast");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damage to Shocker's controller makes that controller discard and draw")
    void damageToControllerWheelsControllersHand() {
        Permanent shocker = addCreatureReady(player1, new Shocker());
        FightingDrake discardedCard = new FightingDrake();
        Mountain discardedLand = new Mountain();
        Mountain firstDraw = new Mountain();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, List.of(new SoulsFire(), discardedCard, discardedLand));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Mountain()));
        harness.setHand(player2, List.of(new FightingDrake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(shocker.getId(), player1.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard, discardedLand);
        harness.assertInGraveyard(player1, "Soul's Fire");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addAttackingShocker(Player player) {
        Permanent shocker = addCreatureReady(player, new Shocker());
        shocker.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
