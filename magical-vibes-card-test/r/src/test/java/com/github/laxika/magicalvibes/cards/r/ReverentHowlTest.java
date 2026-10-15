package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReverentHowl.class, Forest.class, GrizzlyBears.class})
class ReverentHowlTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode makes the target player draw two cards and lose 2 life")
    void drawAndLoseLifeMode() {
        harness.setHand(player1, List.of(new ReverentHowl()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        addManaForSpell();

        harness.castModalInstant(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The second mode gives a creature +2/+2 and lifelink until end of turn")
    void boostsCreatureAndGrantsLifelink() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentHowl()));
        addManaForSpell();

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Each mode enforces its target type")
    void modesRejectWrongTargetTypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentHowl()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first mode can target its caster without affecting the opponent")
    void drawModeCanTargetCaster() {
        harness.setHand(player1, List.of(new ReverentHowl()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        addManaForSpell();

        harness.castModalInstant(player1, 0, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Reverent Howl");
    }

    @Test
    @DisplayName("The creature mode rejects a noncreature permanent")
    void creatureModeRejectsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ReverentHowl()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature mode does nothing when its target leaves before resolution")
    void creatureModeDoesNotResolveWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentHowl()));
        harness.setHand(player2, List.of());
        addManaForSpell();

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Reverent Howl");
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
