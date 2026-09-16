package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({TribalUnity.class, ElvishWarrior.class, GlorySeeker.class})
class TribalUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature type gets +X/+X on every battlefield")
    void boostsChosenTypeByPaidX() {
        Permanent ownElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent ownSoldier = addCreatureReady(player1, new GlorySeeker());
        Permanent opponentElf = addCreatureReady(player2, new ElvishWarrior());

        harness.setHand(player1, List.of(new TribalUnity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSoldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("A matching creature entering after resolution is not affected")
    void doesNotBoostMatchingCreatureEnteringAfterResolution() {
        harness.setHand(player1, List.of(new TribalUnity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent laterElf = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());

        assertThat(gqs.getEffectivePower(gd, laterElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterElf)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());

        harness.setHand(player1, List.of(new TribalUnity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }
}
