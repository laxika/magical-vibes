package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentGuest.class, SerraAngel.class, DoomedDissenter.class})
class BelligerentGuestTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Blood token when it deals combat damage to a player")
    void createsBloodTokenOnCombatDamageToPlayer() {
        Permanent guest = addCreatureReady(player1, new BelligerentGuest());
        guest.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Blood token when blocked without dealing player damage")
    void doesNotCreateBloodTokenWhenBlocked() {
        Permanent guest = addCreatureReady(player1, new BelligerentGuest());
        guest.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Trample damage creates one Blood token regardless of the damage amount")
    void trampleDamageCreatesOneBloodToken() {
        harness.setLife(player2, 20);
        Permanent guest = addCreatureReady(player1, new BelligerentGuest());
        guest.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DoomedDissenter());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller creates the Blood token when player two attacks")
    void tokenBelongsToAttackingController() {
        Permanent guest = addCreatureReady(player2, new BelligerentGuest());
        guest.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Created Blood can be sacrificed with mana and a discarded card to draw")
    void bloodTokenCanDrawACard() {
        Permanent guest = addCreatureReady(player1, new BelligerentGuest());
        guest.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        Card discarded = new BelligerentGuest();
        Card drawn = new BelligerentGuest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
