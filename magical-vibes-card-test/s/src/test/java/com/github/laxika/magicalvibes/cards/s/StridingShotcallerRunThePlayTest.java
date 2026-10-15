package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RunThePlay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.PlayCardRequestDispatchService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StridingShotcallerRunThePlay.class, RunThePlay.class, GrizzlyBears.class})
class StridingShotcallerRunThePlayTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when one or more creatures you control deal combat damage")
    void becomesPreparedAfterAllyCombatDamage() {
        Permanent shotcaller = addShotcaller();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(shotcaller.isPrepared()).isTrue();
        UUID preparedSpellId = shotcaller.getPreparedSpellCardId();
        assertThat(preparedSpellId).isNotNull();
        assertThat(gd.findExiledCard(preparedSpellId).card().getName()).isEqualTo("Run the Play");
    }

    @Test
    @DisplayName("Casting Run the Play puts a counter and flying on its target and draws a card")
    void castsPreparedSpell() {
        Permanent shotcaller = addShotcaller();
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID preparedSpellId = shotcaller.getPreparedSpellCardId();
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCardFromExile(gd, player1, preparedSpellId, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shotcaller.isPrepared()).isFalse();
        assertThat(shotcaller.getPreparedSpellCardId()).isNull();
    }

    private Permanent addShotcaller() {
        return addCreatureReady(player1, new StridingShotcallerRunThePlay());
    }

    @Test
    void canPrepareFromItsOwnCombatDamage() {
        Permanent shotcaller = addShotcaller();
        shotcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(shotcaller.isPrepared()).isTrue();
    }

    @Test
    void opponentsCombatDamageDoesNotPrepareShotcaller() {
        Permanent shotcaller = addShotcaller();
        shotcaller.tap();
        Permanent attacker = addCreatureReady(player2, new StridingShotcallerRunThePlay());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(shotcaller.isPrepared()).isFalse();
        assertThat(attacker.isPrepared()).isTrue();
    }

    @Test
    void zeroXAndNoTargetsStillDrawsACard() {
        Permanent shotcaller = prepareShotcallerForCasting();
        UUID spellId = shotcaller.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, spellId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shotcaller.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(spellId)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void positiveXMayAlsoChooseNoTargets() {
        Permanent shotcaller = prepareShotcallerForCasting();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        gs.playCardFromExile(gd, player1, shotcaller.getPreparedSpellCardId(), 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shotcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shotcaller.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void zeroXCannotChooseACreatureTarget() {
        Permanent shotcaller = prepareShotcallerForCasting();
        Permanent target = addCreatureReady(player2, new StridingShotcallerRunThePlay());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1,
                shotcaller.getPreparedSpellCardId(), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shotcaller.isPrepared()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void flyingExpiresButCounterRemainsAndShotcallerCanPrepareAgain() {
        Permanent shotcaller = prepareShotcallerForCasting();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, shotcaller.getPreparedSpellCardId(), 1, shotcaller.getId());
        harness.passBothPriorities();

        assertThat(shotcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(shotcaller.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(shotcaller.isPrepared()).isFalse();

        advanceToUpkeep(player1);
        assertThat(shotcaller.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(shotcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(shotcaller.isPrepared()).isTrue();
    }

    @Test
    void normalCastRequestAppliesToEveryChosenTarget() {
        Permanent shotcaller = prepareShotcallerForCasting();
        Permanent other = addCreatureReady(player2, new StridingShotcallerRunThePlay());
        Permanent unchosen = addCreatureReady(player1, new StridingShotcallerRunThePlay());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);
        PlayCardRequest request = new PlayCardRequest(
                0, 3, shotcaller.getId(), null,
                List.of(shotcaller.getId(), other.getId()), null, null, null,
                null, shotcaller.getPreparedSpellCardId(), null, null,
                null, null, null, null,
                null, null, null, null, null);

        new PlayCardRequestDispatchService(gs).dispatch(gd, player1, request);
        harness.passBothPriorities();

        assertThat(shotcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shotcaller.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(other.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unchosen.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shotcaller.isPrepared()).isFalse();
    }

    private Permanent prepareShotcallerForCasting() {
        Permanent shotcaller = addShotcaller();
        shotcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(shotcaller.isPrepared()).isTrue();
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shotcaller;
    }
}
