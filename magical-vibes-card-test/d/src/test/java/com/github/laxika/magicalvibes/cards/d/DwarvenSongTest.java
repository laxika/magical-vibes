package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
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

@CardUsed({DwarvenSong.class, BarbaryApes.class, Pendelhaven.class})
class DwarvenSongTest extends BaseCardTest {

    @Test
    @DisplayName("Makes one or more target creatures red until end of turn")
    void makesAllTargetsRed() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        Permanent untargetedCreature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, opposingCreature)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, untargetedCreature)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        cast(List.of(creature.getId()));

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.RED);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Requires at least one target creature")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent pendelhaven = harness.addToBattlefieldAndReturn(player2, new Pendelhaven());
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(pendelhaven.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new BarbaryApes()))
                .toList();

        cast(creatures.stream().map(Permanent::getId).toList());

        for (Permanent creature : creatures) {
            assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.RED);
        }
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still changes a legal target when another target leaves the battlefield")
    void resolvesForRemainingLegalTarget() {
        Permanent departingCreature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(departingCreature.getId(), remainingCreature.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, departingCreature);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, remainingCreature)).containsExactly(CardColor.RED);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not resolve when every target has left the battlefield")
    void doesNotResolveWithNoLegalTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(target.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, untargeted)).containsExactly(CardColor.GREEN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DwarvenSong);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DwarvenSong()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
