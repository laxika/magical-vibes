package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AetherCharge;
import com.github.laxika.magicalvibes.cards.d.DreamChisel;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Naturalize2.class, AetherCharge.class, DreamChisel.class, GlorySeeker.class})
class Naturalize2Test extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new DreamChisel());
        cast(harness.getPermanentId(player2, "Dream Chisel"));

        harness.assertNotOnBattlefield(player2, "Dream Chisel");
        harness.assertInGraveyard(player2, "Dream Chisel");
    }

    @Test
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AetherCharge());
        cast(harness.getPermanentId(player2, "Aether Charge"));

        harness.assertNotOnBattlefield(player2, "Aether Charge");
        harness.assertInGraveyard(player2, "Aether Charge");
    }

    @Test
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new DreamChisel());
        cast(harness.getPermanentId(player1, "Dream Chisel"));

        harness.assertNotOnBattlefield(player1, "Dream Chisel");
        harness.assertInGraveyard(player1, "Dream Chisel");
    }

    @Test
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new Naturalize2()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Glory Seeker");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new Naturalize2()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
