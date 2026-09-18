package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlinkmothNexus.class, CrazedGoblin.class})
class BlinkmothNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Blinkmoth Nexus produces colorless mana")
    void tappingProducesColorlessMana() {
        addCreatureReady(player1, new BlinkmothNexus());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{1} makes Blinkmoth Nexus a 1/1 artifact creature with flying")
    void animateMakesItACreature() {
        Permanent nexus = addCreatureReady(player1, new BlinkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.isArtifact(gd, nexus)).isTrue();
        assertThat(gqs.isLand(gd, nexus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nexus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nexus)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, nexus, CardSubtype.BLINKMOTH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nexus, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{1}, {T} gives a Blinkmoth creature +1/+1")
    void pumpBoostsBlinkmothCreature() {
        Permanent nexus = addCreatureReady(player1, new BlinkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, nexus.getId());
        harness.passBothPriorities();

        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, nexus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nexus)).isEqualTo(2);
    }

    @Test
    @DisplayName("{1}, {T} cannot target a non-Blinkmoth creature")
    void pumpCannotTargetNonBlinkmothCreature() {
        addCreatureReady(player1, new BlinkmothNexus());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, goblin.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("{1}, {T} cannot target an unanimated Blinkmoth Nexus")
    void pumpCannotTargetUnanimatedBlinkmoth() {
        addCreatureReady(player1, new BlinkmothNexus());
        Permanent target = addCreatureReady(player1, new BlinkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Blinkmoth Nexus stops being a creature at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent nexus = addCreatureReady(player1, new BlinkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, nexus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(gqs.isArtifact(gd, nexus)).isFalse();
        assertThat(gqs.isLand(gd, nexus)).isTrue();
        assertThat(gqs.hasKeyword(gd, nexus, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, nexus, CardSubtype.BLINKMOTH)).isFalse();
    }
}
