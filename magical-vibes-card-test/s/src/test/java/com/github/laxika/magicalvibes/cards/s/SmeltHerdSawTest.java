package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmeltHerdSaw.class, FountainOfYouth.class, GrizzlyBears.class})
class SmeltHerdSawTest extends BaseCardTest {

    private static final int SMELT = 0;
    private static final int HERD = 1;
    private static final int SAW = 2;

    @Test
    @DisplayName("Smelt destroys a target artifact")
    void smeltDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(SMELT, List.of(artifact.getId()), ManaColor.RED, 1);

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Smelt cannot target a creature")
    void smeltCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, SMELT, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Herd creates three Elk tokens")
    void herdCreatesElkTokens() {
        cast(HERD, List.of(), ManaColor.GREEN, 1, ManaColor.COLORLESS, 5);

        assertThat(findPermanents(player1, "Elk")).hasSize(3);
    }

    @Test
    @DisplayName("Saw destroys a creature and gives its controller two Half tokens")
    void sawDestroysCreatureAndCreatesHalfTokensForItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(SAW, List.of(creature.getId()), ManaColor.BLACK, 1, ManaColor.COLORLESS, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Half")).hasSize(2);
    }

    @Test
    @DisplayName("Saw cannot target an artifact")
    void sawCannotTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, SAW, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void herdCannotBeCastDuringUpkeep() {
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, HERD, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sawCannotBeCastDuringUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, SAW, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void smeltCanBeCastDuringUpkeep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.forceStep(TurnStep.UPKEEP);

        cast(SMELT, List.of(artifact.getId()), ManaColor.RED, 1);

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void herdIsASorcerySpellOnTheStack() {
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, HERD, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.passBothPriorities();
    }

    @Test
    void sawIsASorcerySpellOnTheStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, SAW, List.of(creature.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.passBothPriorities();
    }

    @Test
    void sawCanTargetOwnCreatureAndGivesTokensToItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(SAW, List.of(creature.getId()), ManaColor.BLACK, 1, ManaColor.COLORLESS, 1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Half")).hasSize(2);
        assertThat(findPermanents(player2, "Half")).isEmpty();
    }

    private void cast(int mode, List<UUID> targets, ManaColor firstColor, int firstAmount,
                      ManaColor secondColor, int secondAmount) {
        harness.setHand(player1, List.of(new SmeltHerdSaw()));
        harness.addMana(player1, firstColor, firstAmount);
        if (secondColor != null) {
            harness.addMana(player1, secondColor, secondAmount);
        }

        harness.castModalInstant(player1, 0, mode, targets);
        harness.passBothPriorities();
    }

    private void cast(int mode, List<UUID> targets, ManaColor firstColor, int firstAmount) {
        cast(mode, targets, firstColor, firstAmount, null, 0);
    }
}
