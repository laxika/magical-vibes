package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessAirStrike.class, SuntailHawk.class, GrizzlyBears.class, Millstone.class, AirElemental.class})
class RecklessAirStrikeTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Deal 3 damage to target creature with flying")
    @CardUsed({RecklessAirStrike.class, SuntailHawk.class, GrizzlyBears.class, AirElemental.class})
    class DamageMode {
        @Test
        @DisplayName("Marks exactly three damage on a surviving flying creature")
        void marksExactlyThreeDamage() {
            Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveSorcery(player1, 0, 0, elemental.getId());

            harness.assertOnBattlefield(player2, "Air Elemental");
            assertThat(elemental.getMarkedDamage()).isEqualTo(3);
            harness.assertInGraveyard(player1, "Reckless Air Strike");
        }

        @Test
        @DisplayName("Damage mode cannot target a player")
        void cannotTargetPlayer() {
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Deals 3 damage to target creature with flying")
        void deals3DamageToFlyingCreature() {
            Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveSorcery(player1, 0, 0, hawk.getId());

            harness.assertNotOnBattlefield(player2, "Suntail Hawk");
            harness.assertInGraveyard(player2, "Suntail Hawk");
        }

        @Test
        @DisplayName("Cannot target a creature without flying with the damage mode")
        void cannotTargetCreatureWithoutFlying() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    @CardUsed({RecklessAirStrike.class, Millstone.class, GrizzlyBears.class})
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveSorcery(player1, 0, 1, millstone.getId());

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with the destroy mode")
        void cannotTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RecklessAirStrike()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
