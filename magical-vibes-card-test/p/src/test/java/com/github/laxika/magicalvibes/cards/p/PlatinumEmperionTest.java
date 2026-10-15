package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.n.NecrogenScudder;
import com.github.laxika.magicalvibes.cards.t.TradingPost;
import com.github.laxika.magicalvibes.cards.w.WhitesunsPassage;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlatinumEmperion.class, Shock.class, WhitesunsPassage.class,
        NecrogenScudder.class, IchorRats.class, TradingPost.class})
class PlatinumEmperionTest extends BaseCardTest {

    

    @Test
    @DisplayName("Damage does not change controller's life total")
    void damageDoesNotChangeLife() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage changes life normally after Platinum Emperion is removed")
    void damageChangesLifeAfterRemoval() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 20);

        // Remove Platinum Emperion
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent's Platinum Emperion does not protect you")
    void opponentEmperionDoesNotProtect() {
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life total stays unchanged even when it would drop to 0")
    void lifeStaysUnchangedAtLethalDamage() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        // Life total stays at 1 — the damage doesn't change it
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Life gain is also prevented")
    void lifeGainPrevented() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 10);

        // Directly test via the query service
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLifeChange(gd, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Resolving a life gain spell cannot change the controller's life")
    void lifeGainSpellDoesNotChangeLife() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new WhitesunsPassage(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Whitesun's Passage");
    }

    @Test
    @DisplayName("Non-damage life loss is blocked without stopping the creature spell")
    void nonDamageLifeLossDoesNotChangeLife() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 2);

        harness.castFromHand(player1, new NecrogenScudder(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertOnBattlefield(player1, "Necrogen Scudder");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Infect damage still gives poison counters while life cannot change")
    void infectDamageStillGivesPoisonCounters() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        Permanent rats = harness.addToBattlefieldAndReturn(player2, new IchorRats());
        rats.setSummoningSick(false);
        rats.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonzero life cost cannot be paid while life cannot change")
    void nonzeroLifeCostCannotBePaid() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new TradingPost());
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't pay life");

        harness.assertLife(player1, 20);
        assertThat(post.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
