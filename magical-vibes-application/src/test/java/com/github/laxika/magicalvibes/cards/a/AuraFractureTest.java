package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuraFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land destroys target enchantment")
    void sacrificingLandDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Aura Fracture cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aura Fracture requires a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
