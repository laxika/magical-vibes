package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skullcrack.class, RenewedFaith.class, DomriRade.class, GreensideWatcher.class,
        ShieldedPassage.class, Mugging.class})
class SkullcrackTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player and locks life gain / damage prevention for the turn")
    void deals3AndLocksLifeGainAndPrevention() {
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playersCantGainLifeThisTurn).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    @DisplayName("A life gain spell resolved afterwards gains nothing")
    void laterLifeGainIsIgnored() {
        harness.setHand(player1, List.of(new Skullcrack(), new RenewedFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Renewed Faith");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The life gain lock wears off at end of turn")
    void lockClearedAtEndOfTurn() {
        gd.playersCantGainLifeThisTurn = true;

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersCantGainLifeThisTurn).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void removesThreeLoyaltyFromPlaneswalker() {
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, domri.getId());

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalTargetStopsBothGlobalRestrictions() {
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, domri.getId());

        domri.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skullcrack");
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isTrue();
    }

    @Test
    void stopsPreventionEstablishedBeforeAndAfterResolutionForEitherPlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        castPassage(player1, first);

        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        castPassage(player2, second);
        castMugging(player1, second);
        castMugging(player1, first);

        harness.assertInGraveyard(player1, "Greenside Watcher");
        harness.assertInGraveyard(player2, "Greenside Watcher");
    }

    @Test
    void resolvedRestrictionsBothExpireAndLifeGainWorksAgain() {
        harness.setHand(player1, List.of(new Skullcrack(), new RenewedFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gqs.isDamagePreventable(gd)).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player1, 26);
    }

    @Test
    void opponentCanGainLifeInResponseButNotAfterResolution() {
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.setHand(player2, List.of(new RenewedFaith(), new RenewedFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player2, 26);

        harness.passBothPriorities();
        harness.assertLife(player2, 23);

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player2, 23);
    }

    private void castPassage(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new ShieldedPassage()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void castMugging(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Mugging()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveSorcery(caster, 0, target.getId());
    }
}
