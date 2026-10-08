package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BiblioplexAssistant;
import com.github.laxika.magicalvibes.cards.c.CogworkArchivist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tangletrap.class, AirElemental.class, GrizzlyBears.class, Millstone.class,
        BiblioplexAssistant.class, CogworkArchivist.class})
class TangletrapTest extends BaseCardTest {

    @Nested
    @CardUsed({Tangletrap.class, AirElemental.class, GrizzlyBears.class})
    @DisplayName("Mode 0: Deal 5 damage to target creature with flying")
    class DamageMode {

        @Test
        @DisplayName("Deals 5 damage to a flying creature")
        void dealsDamageToFlyingCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Air Elemental"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetNonFlyingCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({Tangletrap.class, Millstone.class, GrizzlyBears.class})
    @DisplayName("Mode 1: Destroy target artifact")
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Millstone"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void marksExactlyFiveDamageOnSurvivingFlyer() {
        var flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        flyer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 0, flyer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(flyer.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void canDamageOwnFlyingArtifactCreature() {
        harness.addToBattlefield(player1, new BiblioplexAssistant());
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player1, "Biblioplex Assistant"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Biblioplex Assistant");
        harness.assertInGraveyard(player1, "Biblioplex Assistant");
    }

    @Test
    void destroysOwnNonFlyingArtifactCreature() {
        harness.addToBattlefield(player1, new CogworkArchivist());
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 1,
                harness.getPermanentId(player1, "Cogwork Archivist"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cogwork Archivist");
        harness.assertInGraveyard(player1, "Cogwork Archivist");
    }

    @Test
    void reachDoesNotQualifyForFlyingDamageMode() {
        harness.addToBattlefield(player2, new CogworkArchivist());
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Cogwork Archivist")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Tangletrap.class, BiblioplexAssistant.class})
    void destroysFlyingArtifactEvenWhenFiveDamageWouldNotKillIt() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new BiblioplexAssistant());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Biblioplex Assistant");
        harness.assertInGraveyard(player2, "Biblioplex Assistant");
        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({Tangletrap.class, BiblioplexAssistant.class})
    void damageModeDoesNotDestroyArtifactThatLosesFlyingBeforeResolution() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new BiblioplexAssistant());
        harness.setHand(player1, List.of(new Tangletrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 0, artifact.getId());
        artifact.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Biblioplex Assistant");
        assertThat(artifact.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tangletrap");
    }
}
