package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RerouteSystems.class, DoomBlade.class, FountainOfYouth.class, GrizzlyBears.class, Shatter.class})
class RerouteSystemsTest extends BaseCardTest {

    @Nested
    @CardUsed({RerouteSystems.class, DoomBlade.class, FountainOfYouth.class, GrizzlyBears.class, Shatter.class})
    @DisplayName("Indestructible mode")
    class IndestructibleMode {

        @Test
        @DisplayName("Protects a creature from destruction")
        void protectsCreatureFromDestruction() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new RerouteSystems(), new DoomBlade()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            harness.castAndResolveInstant(player1, 0, bears.getId());

            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Protects an artifact from destruction")
        void protectsArtifactFromDestruction() {
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new RerouteSystems(), new Shatter()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            java.util.UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

            harness.castInstant(player1, 0, 0, fountainId);
            harness.passBothPriorities();

            harness.castAndResolveInstant(player1, 0, fountainId);

            harness.assertOnBattlefield(player1, "Fountain of Youth");
        }

        @Test
        void protectsOpponentsCreatureFromLethalDamageWithoutPreventingDamage() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            bears.tap();
            harness.setHand(player1, List.of(new RerouteSystems(), new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();
            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(bears.getMarkedDamage()).isEqualTo(2);
        }

        @Test
        void indestructibleExpiresAtCleanup() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

            harness.passUntil(player1, TurnStep.CLEANUP);

            assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }
    }

    @Nested
    @CardUsed({RerouteSystems.class, FountainOfYouth.class, GrizzlyBears.class})
    @DisplayName("Damage mode")
    class DamageMode {

        @Test
        @DisplayName("Deals 2 damage to a tapped creature")
        void damagesTappedCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            bears.tap();
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target an untapped creature")
        void cannotTargetUntappedCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a noncreature artifact")
        void cannotTargetArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Fountain of Youth")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotDamageTappedNoncreatureArtifact() {
            Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
            fountain.tap();
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, fountain.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void doesNotDamageCreatureThatUntapsBeforeResolution() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            bears.tap();
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            bears.untap();
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(bears.getMarkedDamage()).isZero();
            harness.assertInGraveyard(player1, "Reroute Systems");
        }

        @Test
        void canDamageTappedCreatureControlledByCaster() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            bears.tap();
            harness.setHand(player1, List.of(new RerouteSystems()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }
    }
}
