package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormShakerOfSkies.class, AirElemental.class, GrizzlyBears.class,
        LeoninScimitar.class, PhyrexianArena.class})
class StormShakerOfSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an artifact")
    void etbDestroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castStorm(target);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB destroys an enchantment")
    void etbDestroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        castStorm(target);

        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("ETB destroys a creature with flying")
    void etbDestroysFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castStorm(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("ETB cannot target a creature without flying")
    void etbCannotTargetNonflyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StormShakerOfSkies()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("ETB can resolve without choosing a target")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new StormShakerOfSkies()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Storm, Shaker of Skies");
    }

    private void castStorm(Permanent target) {
        harness.setHand(player1, List.of(new StormShakerOfSkies()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
