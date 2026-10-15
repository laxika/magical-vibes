package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WakeThrasher;
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

@CardUsed({TraitorousInstinct.class, GrizzlyBears.class, Pacifism.class, WakeThrasher.class})
class TraitorousInstinctTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new TraitorousInstinct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving untaps, steals, pumps +2/+0 and grants haste")
    void resolvesFullPackage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castOn(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Control, haste and the +2/+0 boost all wear off at end of turn")
    void everythingExpiresAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castOn(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new TraitorousInstinct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Gain control precedes untapping, so only the new controller's Wake Thrasher triggers")
    void untapTriggersUseNewController() {
        Permanent friendlyThrasher = addCreatureReady(player1, new WakeThrasher());
        Permanent opposingThrasher = addCreatureReady(player2, new WakeThrasher());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castOn(target);
        resolveAllTriggers();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, friendlyThrasher)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, friendlyThrasher)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, opposingThrasher)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, opposingThrasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("An untapped creature you already control is a legal target")
    void canTargetOwnUntappedCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castOn(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The spell has no effect when its only target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castOn(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        harness.assertInGraveyard(player1, "Traitorous Instinct");
    }
}
