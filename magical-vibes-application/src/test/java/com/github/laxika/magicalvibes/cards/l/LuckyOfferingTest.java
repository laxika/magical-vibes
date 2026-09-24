package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.d.DreamstoneHedron;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuckyOffering.class, ChromaticLantern.class, DreamstoneHedron.class, GrizzlyBears.class})
class LuckyOfferingTest extends BaseCardTest {

    @Test
    void destroysArtifactWithManaValueThreeAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chromatic Lantern");
        harness.assertInGraveyard(player2, "Chromatic Lantern");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void cannotTargetArtifactWithManaValueGreaterThanThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamstoneHedron());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact with mana value 3 or less");
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact with mana value 3 or less");
    }
}
