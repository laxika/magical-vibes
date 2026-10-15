package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SearingBlood;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.cards.t.TempleOfPlenty;
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

@CardUsed({MischiefAndMayhem.class, SwordwiseCentaur.class, TempleOfPlenty.class, SearingBlood.class})
class MischiefAndMayhemTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Both target creatures get +4/+4")
    void twoTargetsGetBoosted() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("May resolve with zero targets even when creatures are available")
    void zeroTargetsAllowed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mischief and Mayhem");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void duplicateTargetRejected() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void threeTargetsRejected() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Remaining legal target is boosted when the other leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.setHand(player2, List.of(new SearingBlood()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, first.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Swordwise Centaur");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Mischief and Mayhem");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new TempleOfPlenty());
        harness.setHand(player1, List.of(new MischiefAndMayhem()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
