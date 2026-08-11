package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessAirStrikeTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Deal 3 damage to target creature with flying")
    class DamageMode {

        @Test
        @DisplayName("Deals 3 damage to target creature with flying")
        void deals3DamageToFlyingCreature() {
            harness.addToBattlefield(player2, new SuntailHawk());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent hawk = findPermanent(player2, "Suntail Hawk");

            harness.castSorcery(player1, 0, 0, hawk.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Suntail Hawk");
            harness.assertInGraveyard(player2, "Suntail Hawk");
        }

        @Test
        @DisplayName("Cannot target a creature without flying with the damage mode")
        void cannotTargetCreatureWithoutFlying() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent millstone = findPermanent(player2, "Millstone");

            harness.castSorcery(player1, 0, 1, millstone.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with the destroy mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
