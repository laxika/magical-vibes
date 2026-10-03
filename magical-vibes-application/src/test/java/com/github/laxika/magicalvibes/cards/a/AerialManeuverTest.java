package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AerialManeuver.class, ScabClanCharger.class, PropheticPrism.class, TotallyLost.class})
class AerialManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Aerial Maneuver gives +1/+1, flying, and first strike to target creature")
    void resolvesAllEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Aerial Maneuver effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Aerial Maneuver cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Prophetic Prism");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Aerial Maneuver can target an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(other.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Aerial Maneuver does not resolve when its target leaves the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        harness.setHand(player1, List.of(new AerialManeuver()));
        harness.setHand(player2, List.of(new TotallyLost()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aerial Maneuver");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(other.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
}
