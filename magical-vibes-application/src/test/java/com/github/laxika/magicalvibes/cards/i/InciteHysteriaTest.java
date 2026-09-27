package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariBrownscale;
import com.github.laxika.magicalvibes.cards.g.GolgariRotwurm;
import com.github.laxika.magicalvibes.cards.m.Moroii;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InciteHysteria.class, ViashinoFangtail.class, GolgariBrownscale.class, GlassGolem.class,
        GolgariRotwurm.class, Moroii.class})
class InciteHysteriaTest extends BaseCardTest {

    @Test
    @DisplayName("Target and every creature sharing a color with it can't block this turn")
    void targetAndColorSharingCreaturesCantBlock() {
        Permanent target = addCreatureReady(player1, new ViashinoFangtail());
        Permanent ownMatchingCreature = addCreatureReady(player1, new ViashinoFangtail());
        Permanent opponentMatchingCreature = addCreatureReady(player2, new ViashinoFangtail());
        Permanent differentColorCreature = addCreatureReady(player2, new GolgariBrownscale());
        Permanent colorlessCreature = addCreatureReady(player2, new GlassGolem());

        castInciteHysteria(target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(ownMatchingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opponentMatchingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(differentColorCreature.isCantBlockThisTurn()).isFalse();
        assertThat(colorlessCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A colorless target affects only itself")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = addCreatureReady(player1, new GlassGolem());
        Permanent otherColorlessCreature = addCreatureReady(player2, new GlassGolem());
        Permanent coloredCreature = addCreatureReady(player2, new ViashinoFangtail());

        castInciteHysteria(target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(otherColorlessCreature.isCantBlockThisTurn()).isFalse();
        assertThat(coloredCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A multicolored target affects creatures sharing either of its colors")
    void multicoloredTargetAffectsCreaturesSharingEitherColor() {
        Permanent target = addCreatureReady(player1, new GolgariRotwurm());
        Permanent greenCreature = addCreatureReady(player2, new GolgariBrownscale());
        Permanent blackCreature = addCreatureReady(player2, new Moroii());
        Permanent differentColorCreature = addCreatureReady(player2, new ViashinoFangtail());

        castInciteHysteria(target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(greenCreature.isCantBlockThisTurn()).isTrue();
        assertThat(blackCreature.isCantBlockThisTurn()).isTrue();
        assertThat(differentColorCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Determines color-sharing creatures when the spell resolves")
    void determinesColorSharingCreaturesOnResolution() {
        Permanent target = addCreatureReady(player1, new ViashinoFangtail());
        castInciteHysteriaWithoutResolving(target);
        Permanent creatureEnteringBeforeResolution = addCreatureReady(player2, new ViashinoFangtail());
        Permanent differentColorCreature = addCreatureReady(player2, new GolgariBrownscale());

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(creatureEnteringBeforeResolution.isCantBlockThisTurn()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(creatureEnteringBeforeResolution.isCantBlockThisTurn()).isTrue();
        assertThat(differentColorCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Does nothing if the target leaves before resolution")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new ViashinoFangtail());
        Permanent matchingCreature = addCreatureReady(player2, new ViashinoFangtail());
        castInciteHysteriaWithoutResolving(target);

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(matchingCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new ViashinoFangtail());

        castInciteHysteria(target);
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    private void castInciteHysteria(Permanent target) {
        castInciteHysteriaWithoutResolving(target);
        harness.passBothPriorities();
    }

    private void castInciteHysteriaWithoutResolving(Permanent target) {
        harness.setHand(player1, List.of(new InciteHysteria()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
    }
}
