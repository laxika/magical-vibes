package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.cards.y.YouHearSomethingOnWatch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Froghemoth.class, NeverwinterDryad.class, HillGiantHerdgorger.class,
        YouHearSomethingOnWatch.class, GroundSeal.class})
class FroghemothTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage sets the exile limit and rewards creature and noncreature cards")
    void combatDamageScalesExileAndRewardsCardTypes() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        HillGiantHerdgorger giant = new HillGiantHerdgorger();
        YouHearSomethingOnWatch instant = new YouHearSomethingOnWatch();
        NeverwinterDryad remaining = new NeverwinterDryad();
        harness.setGraveyard(player2, List.of(dryad, giant, instant, remaining));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(dryad.getId(), giant.getId(), instant.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(dryad, giant, instant);
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Choosing no cards produces no bonuses")
    void choosingNoCardsProducesNoBonuses() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        YouHearSomethingOnWatch instant = new YouHearSomethingOnWatch();
        harness.setGraveyard(player2, List.of(dryad, instant));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(dryad, instant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void emptyDamagedPlayersGraveyardDoesNotUseControllersGraveyard() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        harness.setGraveyard(player1, List.of(dryad));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dryad);
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void missingCreatureTargetDoesNotPreventExilingRemainingNoncreatureTarget() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        YouHearSomethingOnWatch instant = new YouHearSomethingOnWatch();
        harness.setGraveyard(player2, List.of(dryad, instant));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(dryad.getId(), instant.getId()));
        gd.playerGraveyards.get(player2.getId()).remove(dryad);
        gd.playerHands.get(player2.getId()).add(dryad);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player2.getId())).contains(dryad);
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    void sourceLeavingBattlefieldDoesNotPreventExileOrLifeGain() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        YouHearSomethingOnWatch instant = new YouHearSomethingOnWatch();
        harness.setGraveyard(player2, List.of(dryad, instant));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(dryad.getId(), instant.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(froghemoth);
        gd.playerHands.get(player1.getId()).add(froghemoth.getCard());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(dryad, instant);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void graveyardTargetsBecomingUntargetableBeforeResolutionAreNotExiled() {
        NeverwinterDryad dryad = new NeverwinterDryad();
        YouHearSomethingOnWatch instant = new YouHearSomethingOnWatch();
        harness.setGraveyard(player2, List.of(dryad, instant));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(dryad.getId(), instant.getId()));
        harness.addToBattlefield(player2, new GroundSeal());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(dryad, instant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private Permanent addAttackingFroghemoth() {
        Permanent froghemoth = addCreatureReady(player1, new Froghemoth());
        froghemoth.setAttacking(true);
        return froghemoth;
    }
}
