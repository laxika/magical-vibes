package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValorousStanceTest extends BaseCardTest {

    @Nested
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

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Air Elemental"));
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
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Valorous Stance"));
    }
}
