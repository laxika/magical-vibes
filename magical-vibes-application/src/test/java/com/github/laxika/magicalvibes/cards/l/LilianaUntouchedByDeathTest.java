package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaUntouchedByDeathTest extends BaseCardTest {

    @Test
    @DisplayName("+1 mills three cards but drains nothing when no Zombie is milled")
    void plusOneMillsWithoutZombie() {
        Permanent liliana = addReadyLiliana(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 drains each opponent for 2 when a Zombie card is milled")
    void plusOneDrainsOnZombieMilled() {
        addReadyLiliana(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new WalkingCorpse(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("+1 does not drain when the Zombie card is milled outside the top three")
    void plusOneOnlyLooksAtMilledCards() {
        addReadyLiliana(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1,
                List.of(new Forest(), new Forest(), new Forest(), new WalkingCorpse()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-2 shrinks the target by the number of Zombies the activating player controls")
    void minusTwoShrinksByZombieCount() {
        addReadyLiliana(player1, 5);
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 kills a creature when enough Zombies are controlled")
    void minusTwoKillsWithTwoZombies() {
        addReadyLiliana(player1, 5);
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-2 wears off at end of turn")
    void minusTwoWearsOff() {
        addReadyLiliana(player1, 5);
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 lets a Zombie card be cast from the graveyard this turn")
    void minusThreeAllowsZombieGraveyardCast() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, (UUID) null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(perm -> perm.getCard().getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("-3 does not permit casting a non-Zombie card from the graveyard")
    void minusThreeDoesNotAllowNonZombie() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, (UUID) null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 covers Zombie cards that reach the graveyard after it resolves")
    void minusThreeCoversLaterZombies() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, (UUID) null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(perm -> perm.getCard().getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("-3 permission expires at end of turn")
    void minusThreePermissionExpires() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, (UUID) null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 cannot be activated with insufficient loyalty")
    void minusThreeNeedsThreeLoyalty() {
        addReadyLiliana(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Card card = new LilianaUntouchedByDeath();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
