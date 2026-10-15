package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JudgeOfCurrents;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantBasicLandTypeToTargetEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquitectsWill.class, JudgeOfCurrents.class, Mountain.class})
class AquitectsWillTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes an Island in addition to its other types")
    void targetLandBecomesIsland() {
        UUID mountainId = castWillOnMountain();

        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .containsExactlyInAnyOrder(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Target land gains the intrinsic blue mana ability")
    void targetLandCanTapForBlue() {
        UUID mountainId = castWillOnMountain();

        int mountainIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, mountainId));
        harness.activateAbility(player1, mountainIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Island type survives end-of-turn cleanup while the flood counter remains")
    void islandGrantSurvivesTurnReset() {
        UUID mountainId = castWillOnMountain();

        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        mountain.resetModifiers();

        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .containsExactlyInAnyOrder(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, mountain)).contains(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Island type ends when the flood counter is removed")
    void islandGrantEndsWhenFloodCounterRemoved() {
        UUID mountainId = castWillOnMountain();

        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        mountain.setCounterCount(CounterType.FLOOD, 1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);

        mountain.setCounterCount(CounterType.FLOOD, 0);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Grant lives on the permanent, not the shared Card instance")
    void grantDoesNotMutateSharedCard() {
        UUID mountainId = castWillOnMountain();

        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        // The Card object is shared with AI simulation copies and must stay unmodified.
        assertThat(mountain.getCard().getActivatedAbilities()).isEmpty();
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grant resolved in an AI simulation copy does not leak into the real game")
    void simulatedGrantDoesNotLeakIntoRealGame() {
        Permanent realMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        UUID mountainId = realMountain.getId();

        GameData simCopy = gd.simulationCopy();
        Permanent simMountain = simCopy.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(mountainId))
                .findFirst().orElseThrow();
        GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(
                simMountain, CardSubtype.ISLAND, EffectDuration.CONTINUOUS, false);

        assertThat(simMountain.getPersistentGrantedActivatedAbilities()).hasSize(1);
        assertThat(realMountain.getPersistentGrantedActivatedAbilities()).isEmpty();
        assertThat(realMountain.getGrantedSubtypes()).doesNotContain(CardSubtype.ISLAND);
        assertThat(realMountain.getCard().getActivatedAbilities()).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, realMountain)).isEmpty();
    }

    @Test
    @DisplayName("Draws a card when you control a Merfolk")
    void drawsWithMerfolk() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());

        castWillOnMountain(); // hand is set to just the spell, which is spent on cast

        assertThat(handSize()).isEqualTo(1); // drew a card
    }

    @Test
    @DisplayName("Does not draw a card when you control no Merfolk")
    void noDrawWithoutMerfolk() {
        castWillOnMountain(); // hand is set to just the spell, which is spent on cast

        assertThat(handSize()).isZero(); // no card drawn
    }

    @Test
    @DisplayName("An opponent's Merfolk does not satisfy the draw condition")
    void opponentsMerfolkDoesNotAllowDraw() {
        harness.addToBattlefield(player2, new JudgeOfCurrents());

        castWillOnMountain();

        assertThat(handSize()).isZero();
    }

    @Test
    @DisplayName("Can put a flood counter on an opponent's land")
    void canTargetOpponentsLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .containsExactlyInAnyOrder(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);
        assertThat(handSize()).isZero();
    }

    @Test
    @DisplayName("A Merfolk entering before resolution allows the draw")
    void checksMerfolkPresenceAtResolution() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, mountain.getId());

        harness.addToBattlefield(player1, new JudgeOfCurrents());
        harness.passBothPriorities();

        assertThat(handSize()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Merfolk leaving before resolution prevents the draw")
    void noDrawIfMerfolkLeavesBeforeResolution() {
        Permanent judge = harness.addToBattlefieldAndReturn(player1, new JudgeOfCurrents());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, mountain.getId());

        gd.playerBattlefields.get(player1.getId()).remove(judge);
        harness.passBothPriorities();

        assertThat(handSize()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No card is drawn when the only target has left the battlefield")
    void illegalTargetPreventsDrawEvenWithMerfolk() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, mountain.getId());

        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        harness.passBothPriorities();

        assertThat(handSize()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Aquitect's Will");
    }

    @Test
    @DisplayName("Repeated casts add flood counters and the grant lasts until all are removed")
    void repeatedCastsAddCounters() {
        UUID mountainId = castWillOnMountain();
        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, mountainId);

        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(2);
        mountain.setCounterCount(CounterType.FLOOD, 1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);
        mountain.setCounterCount(CounterType.FLOOD, 0);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, mountain)).containsExactly(ManaColor.RED);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        UUID judgeId = harness.addToBattlefieldAndReturn(player2, new JudgeOfCurrents()).getId();
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, judgeId))
                .isInstanceOf(IllegalStateException.class);
    }

    private int handSize() {
        return gd.playerHands.get(player1.getId()).size();
    }

    private UUID castWillOnMountain() {
        UUID mountainId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0, mountainId);
        return mountainId;
    }
}
