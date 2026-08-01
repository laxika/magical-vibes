package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaradGolgariLichLordTest extends BaseCardTest {

    @Nested
    @DisplayName("Static +1/+1 per creature card in graveyard")
    class StaticBoostTests {

        @Test
        @DisplayName("Base 2/2 with empty graveyard")
        void basePtWithEmptyGraveyard() {
            Permanent jarad = addReadyJarad(player1);

            assertThat(gqs.getEffectivePower(gd, jarad)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, jarad)).isEqualTo(2);
        }

        @Test
        @DisplayName("Gets +1/+1 per creature card in controller's graveyard")
        void boostsFromCreatureCardsInGraveyard() {
            Permanent jarad = addReadyJarad(player1);
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

            assertThat(gqs.getEffectivePower(gd, jarad)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, jarad)).isEqualTo(4);
        }

        @Test
        @DisplayName("Does not count non-creature cards or opponent graveyard")
        void ignoresNonCreaturesAndOpponentGraveyard() {
            Permanent jarad = addReadyJarad(player1);
            harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
            harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

            assertThat(gqs.getEffectivePower(gd, jarad)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, jarad)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Sacrifice another creature life-loss ability")
    class SacLifeLossTests {

        @Test
        @DisplayName("Each opponent loses life equal to sacrificed creature's power")
        void opponentsLoseSacrificedPower() {
            addReadyJarad(player1);
            addCreatureReady(player1, new GrizzlyBears());
            harness.setLife(player2, 20);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Uses sacrificed creature's effective power")
        void usesEffectivePower() {
            addReadyJarad(player1);
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            harness.setLife(player2, 20);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Cannot activate when Jarad is the only creature")
        void cannotSacrificeSelf() {
            addReadyJarad(player1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Graveyard return ability")
    class GraveyardAbilityTests {

        @Test
        @DisplayName("Sacrificing a Swamp and a Forest returns Jarad to hand")
        void returnsToHand() {
            JaradGolgariLichLord jarad = new JaradGolgariLichLord();
            harness.setGraveyard(player1, List.of(jarad));
            UUID swampId = harness.addToBattlefieldAndReturn(player1, new Swamp()).getId();
            harness.addToBattlefield(player1, new Forest());

            harness.activateGraveyardAbility(player1, 0);
            // Sequence cost prompts for the Swamp first; sole Forest auto-pays the second slot.
            harness.handlePermanentChosen(player1, swampId);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Jarad, Golgari Lich Lord");
            harness.assertNotInGraveyard(player1, "Jarad, Golgari Lich Lord");
            harness.assertInGraveyard(player1, "Swamp");
            harness.assertInGraveyard(player1, "Forest");
        }

        @Test
        @DisplayName("Cannot activate without both a Swamp and a Forest")
        void cannotActivateWithoutBothLands() {
            JaradGolgariLichLord jarad = new JaradGolgariLichLord();
            harness.setGraveyard(player1, List.of(jarad));
            harness.addToBattlefield(player1, new Swamp());

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addReadyJarad(Player player) {
        return addCreatureReady(player, new JaradGolgariLichLord());
    }
}
