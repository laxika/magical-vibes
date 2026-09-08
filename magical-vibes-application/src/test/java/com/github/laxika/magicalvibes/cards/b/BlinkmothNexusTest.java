package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlinkmothNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Blinkmoth Nexus produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent nexus = addNexusReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(nexus);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{1} makes Blinkmoth Nexus a 1/1 artifact creature with flying")
    void animateMakesItACreature() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.isArtifact(nexus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nexus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nexus)).isEqualTo(1);
        assertThat(nexus.getTransientSubtypes()).containsExactly(CardSubtype.BLINKMOTH);
        assertThat(nexus.getGrantedKeywords()).containsExactly(Keyword.FLYING);
        assertThat(nexus.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("{1}, {T} gives a Blinkmoth creature +1/+1")
    void pumpBoostsBlinkmothCreature() {
        Permanent nexus = addNexusReady(player1);
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
        addNexusReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Blinkmoth Nexus stops being a creature at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, nexus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nexus.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(gqs.isArtifact(nexus)).isFalse();
        assertThat(nexus.getGrantedKeywords()).isEmpty();
        assertThat(nexus.getTransientSubtypes()).isEmpty();
    }

    private Permanent addNexusReady(Player player) {
        Permanent perm = new Permanent(new BlinkmothNexus());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
