package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.v.VampireBats;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanParadise.class, VampireBats.class, Karakas.class})
class SylvanParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Makes one or more target creatures green until end of turn")
    void makesAllTargetsGreen() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VampireBats());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new VampireBats());

        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, opposingCreature)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Leaves untargeted creatures unchanged")
    void leavesUntargetedCreaturesUnchanged() {
        Permanent targetedCreature = harness.addToBattlefieldAndReturn(player1, new VampireBats());
        Permanent untargetedCreature = harness.addToBattlefieldAndReturn(player2, new VampireBats());

        cast(List.of(targetedCreature.getId()));

        assertThat(gqs.getEffectiveColors(gd, targetedCreature)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, untargetedCreature)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VampireBats());

        cast(List.of(creature.getId()));

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Requires at least one target creature")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new SylvanParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());
        harness.setHand(player1, List.of(new SylvanParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(karakas.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new VampireBats()))
                .toList();

        cast(creatures.stream().map(Permanent::getId).toList());

        for (Permanent creature : creatures) {
            assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
        }
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VampireBats());
        harness.setHand(player1, List.of(new SylvanParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not change creatures entering after resolution")
    void doesNotAffectLaterCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireBats());

        cast(List.of(target.getId()));
        Permanent newcomer = harness.addToBattlefieldAndReturn(player2, new VampireBats());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, newcomer)).containsExactly(CardColor.BLACK);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SylvanParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
