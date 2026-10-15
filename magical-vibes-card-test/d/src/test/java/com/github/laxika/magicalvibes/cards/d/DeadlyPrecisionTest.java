package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BendersWaterskin;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyPrecision.class, YuyanArchers.class, BendersWaterskin.class, Plains.class})
class DeadlyPrecisionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and destroys target creature")
    void sacrificesArtifactAndDestroysTargetCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());

        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bender's Waterskin");
        harness.assertNotOnBattlefield(player2, "Yuyan Archers");
    }

    @Test
    @DisplayName("Sacrifices a creature and destroys target creature")
    void sacrificesCreatureAndDestroysTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new YuyanArchers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());

        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yuyan Archers");
        harness.assertNotOnBattlefield(player2, "Yuyan Archers");
    }

    @Test
    @DisplayName("Pays {4} instead of sacrificing and destroys target creature")
    void paysManaInsteadOfSacrificing() {
        harness.addToBattlefield(player1, new BendersWaterskin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());

        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bender's Waterskin");
        harness.assertNotOnBattlefield(player2, "Yuyan Archers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without an artifact or creature or enough mana")
    void cannotCastWithoutPaymentOption() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());

        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsLandTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysAdditionalManaWithoutAnyPermanentToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Yuyan Archers");
        harness.assertInGraveyard(player1, "Deadly Precision");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void sacrificeIsPaidBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), artifact.getId());

        harness.assertNotOnBattlefield(player1, "Bender's Waterskin");
        harness.assertInGraveyard(player1, "Bender's Waterskin");
        harness.assertOnBattlefield(player2, "Yuyan Archers");
        harness.assertNotInGraveyard(player1, "Deadly Precision");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Yuyan Archers");
        harness.assertInGraveyard(player1, "Deadly Precision");
    }

    @Test
    void canSacrificeTheTargetCreatureAsAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YuyanArchers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertInGraveyard(player1, "Yuyan Archers");
        harness.assertNotInHand(player1, "Deadly Precision");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Precision");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BendersWaterskin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Bender's Waterskin");
        harness.assertOnBattlefield(player2, "Yuyan Archers");
        harness.assertInHand(player1, "Deadly Precision");
    }

    @Test
    void cannotSacrificeAnOrdinaryLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Yuyan Archers");
        harness.assertInHand(player1, "Deadly Precision");
    }
}
