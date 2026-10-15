package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeamtownBeatstick;
import com.github.laxika.magicalvibes.cards.c.CutShort;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormclawRager.class, ShivanBranchBurner.class, BeamtownBeatstick.class, CutShort.class})
class StormclawRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Stormclaw Rager and draws a card")
    void sacrificingCreaturePutsCounterAndDrawsCard() {
        Permanent rager = addReadyRager();
        Permanent creature = addCreatureReady(player1, new ShivanBranchBurner());
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        setUpLibraryAndHand();

        harness.activateAbility(player1, 0, 0, null, null);
        choosePermanent(creature);
        harness.passBothPriorities();

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getOriginalCard());
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a counter on Stormclaw Rager and draws a card")
    void sacrificingArtifactPutsCounterAndDrawsCard() {
        Permanent rager = addReadyRager();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BeamtownBeatstick());
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        setUpLibraryAndHand();

        harness.activateAbility(player1, 0, 0, null, null);
        choosePermanent(artifact);
        harness.passBothPriorities();

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getOriginalCard());
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        addReadyRager();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("The source cannot be sacrificed to pay its own ability")
    void cannotSacrificeSource() {
        addReadyRager();
        prepareForSorcerySpeed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
    }

    @Test
    void sacrificeIsPaidBeforeTheAbilityResolves() {
        Permanent rager = addReadyRager();
        Permanent creature = addCreatureReady(player1, new ShivanBranchBurner());
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        setUpLibraryAndHand();

        harness.activateAbility(player1, 0, 0, null, null);
        choosePermanent(creature);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getOriginalCard());
        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedSummoningSickRagerCanActivate() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new StormclawRager());
        rager.setSummoningSick(true);
        rager.tap();
        Permanent creature = addCreatureReady(player1, new ShivanBranchBurner());
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        setUpLibraryAndHand();

        harness.activateAbility(player1, 0, 0, null, null);
        choosePermanent(creature);
        harness.passBothPriorities();

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotSacrificeOpponentsPermanents() {
        addReadyRager();
        addCreatureReady(player2, new ShivanBranchBurner());
        harness.addToBattlefield(player2, new BeamtownBeatstick());
        prepareForSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addReadyRager();
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void stillDrawsWhenRagerIsDestroyedInResponse() {
        Permanent rager = addReadyRager();
        rager.tap();
        Permanent creature = addCreatureReady(player1, new ShivanBranchBurner());
        addCreatureReady(player1, new ShivanBranchBurner());
        prepareForSorcerySpeed();
        setUpLibraryAndHand();
        harness.setHand(player2, List.of(new CutShort()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        choosePermanent(creature);
        harness.castAndResolveInstant(player2, 0, rager.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rager);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rager.getOriginalCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyRager() {
        return addCreatureReady(player1, new StormclawRager());
    }

    private void prepareForSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
    }

    private void setUpLibraryAndHand() {
        harness.setLibrary(player1, List.of(new ShivanBranchBurner()));
        harness.setHand(player1, List.of());
    }

    private void choosePermanent(Permanent permanent) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(permanent.getId());
        harness.handlePermanentChosen(player1, permanent.getId());
    }
}
