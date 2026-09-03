package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CrystalGolem;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
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

@CardUsed({JabarisInfluence.class, IronTuskElephant.class, FeralShadow.class, CrystalGolem.class})
class JabarisInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Steals the attacker and shrinks its power by one")
    void stealsAttackerAndPutsMinusOneMinusZeroCounter() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castInstant(player2, 0, elephant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elephant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elephant);
        // 3/3 base, -1/-0 counter.
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that did not attack this turn")
    void cannotTargetNonAttacker() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elephant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black attacker")
    void cannotTargetBlackAttacker() {
        Permanent shadow = addCreatureReady(player1, new FeralShadow());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shadow.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent golem = addCreatureReady(player1, new CrystalGolem());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, golem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast before combat has ended")
    void cannotCastBeforeCombatEnds() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elephant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
