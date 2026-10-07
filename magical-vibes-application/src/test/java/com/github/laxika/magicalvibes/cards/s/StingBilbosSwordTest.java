package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FearsomeGoblinPair;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingBilbosSword.class, FearsomeGoblinPair.class})
class StingBilbosSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Sting counts creatures controlled by the targeted opponent and attaches to your creature")
    void countsTargetOpponentsCreaturesAndAttaches() {
        Permanent host = addCreatureReady(player1, new FearsomeGoblinPair());
        addCreatureReady(player2, new FearsomeGoblinPair());
        addCreatureReady(player2, new FearsomeGoblinPair());

        castSting(player2.getId(), host.getId());

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(2);
        assertThat(sting.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sting may enter without attaching")
    void mayEnterWithoutAttaching() {
        addCreatureReady(player2, new FearsomeGoblinPair());

        castSting(player2.getId(), null);

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(sting.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The ETB attachment target must be a creature you control")
    void attachmentCannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new FearsomeGoblinPair());

        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), opponentCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castSting(UUID opponentId, UUID creatureId) {
        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<UUID> targetIds = creatureId == null
                ? List.of(opponentId)
                : List.of(opponentId, creatureId);
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of());
        resolveAllTriggers();
    }

    @Test
    void canBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);

        castSting(player2.getId(), null);

        harness.assertOnBattlefield(player1, "Sting, Bilbo's Sword");
    }

    @Test
    void attachesEvenWhenOpponentControlsNoCreatures() {
        Permanent host = addCreatureReady(player1, new FearsomeGoblinPair());

        castSting(player2.getId(), host.getId());

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isZero();
        assertThat(sting.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
    }

    @Test
    void countsCreaturesAtAbilityResolution() {
        addCreatureReady(player2, new FearsomeGoblinPair());
        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());
        harness.passBothPriorities();

        addCreatureReady(player2, new FearsomeGoblinPair());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sting, Bilbo's Sword")
                .getCounterCount(CounterType.HONE)).isEqualTo(2);
    }

    @Test
    void stillGetsCountersWhenAttachmentTargetLeaves() {
        Permanent host = addCreatureReady(player1, new FearsomeGoblinPair());
        addCreatureReady(player2, new FearsomeGoblinPair());
        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), host.getId()), List.of());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(host);
        resolveAllTriggers();

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(sting.getAttachedTo()).isNull();
    }

    @Test
    void equipMovesHoneBonusToNewCreatureWithoutChangingCounters() {
        Permanent first = addCreatureReady(player1, new FearsomeGoblinPair());
        Permanent second = addCreatureReady(player1, new FearsomeGoblinPair());
        addCreatureReady(player2, new FearsomeGoblinPair());
        castSting(player2.getId(), first.getId());
        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sting),
                null, second.getId());
        resolveAllTriggers();

        assertThat(sting.getAttachedTo()).isEqualTo(second.getId());
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }
}
