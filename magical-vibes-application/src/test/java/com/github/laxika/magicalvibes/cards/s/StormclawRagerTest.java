package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({StormclawRager.class, GrizzlyBears.class, LeoninScimitar.class})
class StormclawRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Stormclaw Rager and draws a card")
    void sacrificingCreaturePutsCounterAndDrawsCard() {
        Permanent rager = addReadyRager();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        addCreatureReady(player1, new GrizzlyBears());
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
    }

    private void choosePermanent(Permanent permanent) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(permanent.getId());
        harness.handlePermanentChosen(player1, permanent.getId());
    }
}
