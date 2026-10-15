package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorousStance.class, GrizzlyBears.class, AirElemental.class})
class ValorousStanceTest extends BaseCardTest {

    @Nested
    @CardUsed({ValorousStance.class, GrizzlyBears.class})
    @DisplayName("Mode 0: Target creature gains indestructible until end of turn")
    class IndestructibleMode {

        @Test
        @DisplayName("Grants indestructible to target creature")
        void grantsIndestructible() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new ValorousStance()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        }

        @Test
        @DisplayName("Indestructible wears off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new ValorousStance()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        }
    }

    @Nested
    @CardUsed({ValorousStance.class, GrizzlyBears.class, AirElemental.class})
    @DisplayName("Mode 1: Destroy target creature with toughness 4 or greater")
    class DestroyToughCreatureMode {

        @Test
        @DisplayName("Destroys target creature with toughness 4 or greater")
        void destroysToughCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new ValorousStance()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player2, "Air Elemental");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature with toughness less than 4")
        void cannotTargetLowToughnessCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            // Need a valid target so the spell is castable
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new ValorousStance()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void protectsOpponentsCreatureFromPendingDestruction() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ValorousStance(), new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, 1, targetId);
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Air Elemental");
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void indestructibleModeDoesNotResolveWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ValorousStance(), new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, 0, targetId);
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Valorous Stance");
    }
}
