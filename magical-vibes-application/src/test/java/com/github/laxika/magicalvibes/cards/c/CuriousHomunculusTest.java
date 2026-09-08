package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuriousHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms during its controller's upkeep with three instant or sorcery cards in the graveyard")
    void transformsWithThreeInstantOrSorceryCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        harness.addToBattlefield(player1, new CuriousHomunculus());
        Permanent homunculus = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(homunculus.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform without three instant or sorcery cards in the graveyard")
    void doesNotTransformWithoutThreshold() {
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears(), new Shock()));
        harness.addToBattlefield(player1, new CuriousHomunculus());
        Permanent homunculus = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(homunculus.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Restricted mana casts instant and sorcery spells but not creature spells")
    void restrictedManaIsInstantSorceryOnly() {
        harness.addToBattlefield(player1, new CuriousHomunculus());
        Permanent homunculus = gd.playerBattlefields.get(player1.getId()).getFirst();
        homunculus.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Voracious Reader reduces instant and sorcery costs and has prowess")
    void backFaceReducesSpellCostsAndHasProwess() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        harness.addToBattlefield(player1, new CuriousHomunculus());
        Permanent homunculus = gd.playerBattlefields.get(player1.getId()).getFirst();

        transform(homunculus);
        int powerBeforeCast = gqs.getEffectivePower(gd, homunculus);
        int toughnessBeforeCast = gqs.getEffectiveToughness(gd, homunculus);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(powerBeforeCast + 1);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(toughnessBeforeCast + 1);
    }

    private void transform(Permanent homunculus) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(homunculus.isTransformed()).isTrue();
    }
}
