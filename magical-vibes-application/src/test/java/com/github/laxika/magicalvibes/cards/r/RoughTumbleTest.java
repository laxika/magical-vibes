package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AkromaAngelOfFury;
import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SynchronousSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoughTumble.class, BloodKnight.class, SerraSphinx.class, SynchronousSliver.class,
        AkromaAngelOfFury.class, GaeasAnthem.class})
class RoughTumbleTest extends BaseCardTest {

    @Test
    @DisplayName("Rough deals 2 damage to each creature without flying")
    void roughDamagesOnlyCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new BloodKnight());
        Permanent undamagedCreature = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new SynchronousSliver());
        harness.setHand(player1, List.of(new RoughTumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blood Knight");
        harness.assertOnBattlefield(player2, "Serra Sphinx");
        assertThat(undamagedCreature.getMarkedDamage()).isZero();
        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tumble deals 6 damage to each creature with flying")
    void tumbleDamagesOnlyCreaturesWithFlying() {
        Permanent undamagedCreature = harness.addToBattlefieldAndReturn(player1, new BloodKnight());
        harness.addToBattlefield(player2, new GaeasAnthem());
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new AkromaAngelOfFury());
        harness.setHand(player1, List.of(new RoughTumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood Knight");
        harness.assertOnBattlefield(player2, "Akroma, Angel of Fury");
        assertThat(undamagedCreature.getMarkedDamage()).isZero();
        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Tumble kills flying creatures controlled by either player and leaves other permanents alone")
    void tumbleKillsFlyingCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new SerraSphinx());
        harness.addToBattlefield(player2, new SerraSphinx());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new BloodKnight());
        harness.addToBattlefield(player1, new GaeasAnthem());
        harness.setHand(player1, List.of(new RoughTumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Serra Sphinx");
        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertOnBattlefield(player2, "Blood Knight");
        harness.assertOnBattlefield(player1, "Gaea's Anthem");
        assertThat(groundCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Rough can resolve without creatures and does not damage players")
    void roughResolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new RoughTumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rough // Tumble");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tumble can resolve without creatures and does not damage players")
    void tumbleResolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new RoughTumble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rough // Tumble");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
