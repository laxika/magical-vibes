package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.Expansion;
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

@CardUsed({ArrestersZeal.class, AxebaneBeast.class})
class ArrestersZealTest extends BaseCardTest {

    @Test
    @DisplayName("Addendum applies during your postcombat main phase")
    void grantsFlyingDuringPostcombatMainPhase() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting during an opponent's main phase does not grant flying")
    void doesNotGrantFlyingDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @CardUsed({ArrestersZeal.class, AxebaneBeast.class, Expansion.class})
    @DisplayName("An uncast spell copy does not receive the addendum bonus")
    void copyDoesNotGrantFlyingDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxebaneBeast());
        ArrestersZeal zeal = new ArrestersZeal();
        harness.setHand(player1, List.of(zeal, new Expansion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, 0, zeal.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("During your main phase, target creature gets +2/+2 and flying")
    void grantsFlyingDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Axebane Beast");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Outside your main phase, target creature gets +2/+2 without flying")
    void doesNotGrantFlyingOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Axebane Beast");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AxebaneBeast());
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Axebane Beast");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
