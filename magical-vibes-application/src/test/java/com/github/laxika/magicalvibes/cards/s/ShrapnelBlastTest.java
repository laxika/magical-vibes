package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.l.LeoninDenGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrapnelBlast.class, AlphaMyr.class, AncientDen.class, LeoninDenGuard.class})
class ShrapnelBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target player and sacrifices the artifact")
    void dealsFiveDamageToPlayer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Shrapnel Blast");
    }

    @Test
    @DisplayName("Deals 5 damage to a target creature, killing it")
    void dealsFiveDamageToCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninDenGuard());

        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Den-Guard");
        harness.assertInGraveyard(player2, "Leonin Den-Guard");
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact permanent")
    void cannotSacrificeNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninDenGuard());

        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Does not pay the sacrifice cost when the target is illegal")
    void doesNotPaySacrificeCostWhenTargetIsIllegal() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AncientDen());

        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, land.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Ancient Den");
        harness.assertInHand(player1, "Shrapnel Blast");
    }
}
