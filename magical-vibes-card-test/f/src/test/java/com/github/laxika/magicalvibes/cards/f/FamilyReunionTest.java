package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FamilyReunion.class, GrizzlyBears.class, Shock.class})
class FamilyReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives your creatures +1/+1 and leaves opposing creatures unchanged")
    void boostModeBoostsOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(player1, 0);

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Hexproof mode protects your creatures only")
    void hexproofModeProtectsOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(player1, 1);

        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, ownCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Boost mode wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(player1, 0);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Hexproof mode wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(player1, 1);
        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Each mode affects all creatures present at resolution and excludes later arrivals")
    void affectedCreaturesAreDeterminedAtResolution(int mode) {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FamilyReunion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, mode, List.of());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Permanent affected : List.of(first, beforeResolution)) {
            assertThat(affected.getPowerModifier()).isEqualTo(mode == 0 ? 1 : 0);
            assertThat(affected.getToughnessModifier()).isEqualTo(mode == 0 ? 1 : 0);
            assertThat(affected.hasKeyword(Keyword.HEXPROOF)).isEqualTo(mode == 1);
        }
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
        assertThat(afterResolution.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode can resolve without any creatures")
    void resolvesWithEmptyBattlefield(int mode) {
        cast(player1, mode);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FamilyReunion);
    }

    @Test
    @DisplayName("Hexproof gained in response makes an opponent's spell lose its target")
    void hexproofStopsSpellAlreadyOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());

        cast(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Hexproof does not prevent its controller from targeting the creature")
    void controllerCanStillTargetHexproofCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }
    private void cast(Player player, int mode) {
        harness.setHand(player, List.of(new FamilyReunion()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
