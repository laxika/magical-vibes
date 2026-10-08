package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariBrownscale;
import com.github.laxika.magicalvibes.cards.g.GolgariRotwurm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Moroii;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WojekEmbermage.class, GolgariBrownscale.class, ViashinoFangtail.class,
        GlassGolem.class, Island.class, GolgariRotwurm.class, Moroii.class})
class WojekEmbermageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the target and every creature sharing a color with it")
    void damagesTargetAndColorSharingCreatures() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages matching creatures on both battlefields")
    void damagesMatchingCreaturesOnBothBattlefields() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(ownMatchingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentMatchingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages creatures sharing either color with a multicolored target")
    void damagesCreaturesSharingEitherColorWithMulticoloredTarget() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariRotwurm());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Moroii());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(greenCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(blackCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A colorless target does not damage other colorless creatures")
    void colorlessTargetOnlyDamagesItself() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(otherColorlessCreature.getMarkedDamage()).isZero();
        assertThat(coloredCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Determines color-sharing creatures when the ability resolves")
    void determinesColorSharingCreaturesOnResolution() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());
        Permanent creatureEnteringBeforeResolution =
                harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent differentColorCreature =
                harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(creatureEnteringBeforeResolution.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does nothing if the target leaves before the ability resolves")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(matchingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Taps itself when its ability is activated")
    void tapsItselfWhenActivated() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());

        assertThat(embermage.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent embermage = addReadyEmbermage();
        embermage.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(embermage), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent embermage = harness.addToBattlefieldAndReturn(player1, new WojekEmbermage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(embermage), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent embermage = addReadyEmbermage();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> activate(embermage, island))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself and damages other red creatures")
    void canTargetItself() {
        Permanent embermage = addReadyEmbermage();
        Permanent redCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());

        activate(embermage, embermage);

        assertThat(embermage.getMarkedDamage()).isEqualTo(1);
        assertThat(redCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(greenCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Color sharing does not spread through another multicolored creature")
    void doesNotChainColorSharing() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent greenBlackCreature = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        Permanent blueBlackCreature = harness.addToBattlefieldAndReturn(player2, new Moroii());

        activate(embermage, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(greenBlackCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(blueBlackCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent embermage = addReadyEmbermage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(embermage);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyEmbermage() {
        return addCreatureReady(player1, new WojekEmbermage());
    }

    private void activate(Permanent embermage, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(embermage),
                null, target.getId());
        harness.passBothPriorities();
    }
}
