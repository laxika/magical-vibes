package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeepOutTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 4 damage to a tapped creature")
    void damageModeDealsDamageToTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode destroys a target enchantment")
    void destroyModeDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Damage mode cannot target an untapped creature")
    void damageModeCannotTargetUntappedCreature() {
        Permanent untappedBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent tappedBears = addCreatureReady(player2, new GrizzlyBears());
        tappedBears.tap();

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, untappedBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature")
    void destroyModeCannotTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
