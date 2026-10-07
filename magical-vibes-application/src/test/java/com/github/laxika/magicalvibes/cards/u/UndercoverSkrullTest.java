package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercoverSkrull.class, GrizzlyBears.class, Forest.class})
class UndercoverSkrullTest extends BaseCardTest {

    @Test
    void getsBoostAndAllCreatureTypesWithTwoCreatureCardsInGraveyard() {
        Permanent skrull = addReadySkrull();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skrull)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ZOMBIE)).isTrue();
    }

    @Test
    void doesNotCountNoncreatureOrOpponentCardsForThreshold() {
        Permanent skrull = addReadySkrull();
        int basePower = gqs.getEffectivePower(gd, skrull);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(basePower);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isFalse();

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(basePower);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void tapsForOneManaOfEachChosenColor(ManaColor color) {
        addCreatureReady(player1, new UndercoverSkrull());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadySkrull() {
        return addCreatureReady(player1, new UndercoverSkrull());
    }

    @Test
    void losesBoostAndAddedCreatureTypesWhenGraveyardFallsBelowThreshold() {
        Permanent skrull = addReadySkrull();
        harness.setGraveyard(player1, List.of(new UndercoverSkrull(), new UndercoverSkrull()));

        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skrull)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isTrue();

        harness.setGraveyard(player1, List.of(new UndercoverSkrull()));

        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skrull)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isFalse();
    }

    @Test
    void thresholdAffectsOnlyTheSourceAndUsesItsControllersGraveyard() {
        Permanent ownSkrull = addReadySkrull();
        Permanent opposingSkrull = addCreatureReady(player2, new UndercoverSkrull());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new UndercoverSkrull(), new UndercoverSkrull(),
                new UndercoverSkrull()));

        assertThat(gqs.getEffectivePower(gd, opposingSkrull)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSkrull)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, opposingSkrull, CardSubtype.ELF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownSkrull)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, ownSkrull, CardSubtype.ELF)).isFalse();

        harness.setGraveyard(player1, List.of(new UndercoverSkrull(), new UndercoverSkrull()));

        assertThat(gqs.getEffectivePower(gd, ownSkrull)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isFalse();
    }

    @Test
    void manaAbilityResolvesImmediatelyAndRequiresAnUntappedSource() {
        Permanent skrull = addReadySkrull();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(skrull.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void summoningSickSourceCannotActivateManaAbility() {
        Permanent skrull = harness.addToBattlefieldAndReturn(player1, new UndercoverSkrull());
        skrull.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(skrull.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
