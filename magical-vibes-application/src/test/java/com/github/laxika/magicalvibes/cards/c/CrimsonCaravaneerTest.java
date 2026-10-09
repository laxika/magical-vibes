package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonCaravaneer.class, GrizzlyBears.class, Forest.class})
class CrimsonCaravaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Junk token for each combat damage step that hits a player")
    void createsJunkForEachCombatDamageStep() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Junk when blocked")
    void doesNotCreateJunkWhenBlocked() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).isEmpty();
    }

    @Test
    void junkSacrificesAsACostAndExilesOnlyTheTopCardForPaidCasting() {
        int junkIndex = createJunkAndPrepareMainPhase();
        CrimsonCaravaneer top = new CrimsonCaravaneer();
        CrimsonCaravaneer next = new CrimsonCaravaneer();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, junkIndex, null, null);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, top.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Crimson Caravaneer")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void junkCannotBeActivatedOutsideAMainPhase() {
        int junkIndex = createJunkAndPrepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void junkCannotBeActivatedDuringOpponentsMainPhase() {
        int junkIndex = createJunkAndPrepareMainPhase();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void tappedJunkCannotPayItsActivationCost() {
        int junkIndex = createJunkAndPrepareMainPhase();
        findPermanent(player1, "Junk").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void junkCanBeSacrificedWithAnEmptyLibrary() {
        int junkIndex = createJunkAndPrepareMainPhase();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void junkPlayPermissionExpiresAfterTheTurn() {
        int junkIndex = createJunkAndPrepareMainPhase();
        CrimsonCaravaneer top = new CrimsonCaravaneer();
        harness.setLibrary(player1, List.of(top));
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(countPermanents(player1, "Crimson Caravaneer")).isEqualTo(1);
    }

    private int createJunkAndPrepareMainPhase() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        return gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));
    }

    @Test
    void tramplesToPlayerAfterKillingBlockerInFirstStrikeDamage() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setMarkedDamage(1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, java.util.Map.of(blocker.getId(), 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void junkCannotBeActivatedWithASpellOnTheStack() {
        int junkIndex = createJunkAndPrepareMainPhase();
        harness.castFromHand(player1, new CrimsonCaravaneer(), "{2}{R}");

        assertThatThrownBy(() -> harness.activateAbility(player1, junkIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
        resolveAllTriggers();
    }

    @Test
    void junkAllowsPlayingAnExiledLandButDoesNotGrantAnExtraLandPlay() {
        int junkIndex = createJunkAndPrepareMainPhase();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();
        harness.castFromExile(player1, first.getId());
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);

        int remainingJunk = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Junk"));
        harness.activateAbility(player1, remainingJunk, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }
}
