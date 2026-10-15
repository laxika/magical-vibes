package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.l.LaccolithWhelp;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
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

@CardUsed({FlowstoneStrike.class, RootwaterCommando.class, KorHaven.class, LaccolithWhelp.class})
class FlowstoneStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/-1 and haste until end of turn")
    void boostsAndGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootwaterCommando());
        harness.setHand(player1, List.of(new FlowstoneStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootwaterCommando());
        harness.setHand(player1, List.of(new FlowstoneStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        harness.setHand(player1, List.of(new FlowstoneStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature reduced to zero toughness dies")
    void killsOneToughnessCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LaccolithWhelp());
        harness.setHand(player1, List.of(new FlowstoneStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Laccolith Whelp");
        harness.assertInGraveyard(player2, "Laccolith Whelp");
        harness.assertInGraveyard(player1, "Flowstone Strike");
    }

    @Test
    @DisplayName("Haste allows a newly entered creature to attack")
    void allowsSummoningSickCreatureToAttack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RootwaterCommando());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new FlowstoneStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }
}
