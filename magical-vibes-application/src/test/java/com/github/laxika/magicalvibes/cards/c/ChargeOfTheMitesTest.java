package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargeOfTheMitesTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals damage equal to the number of creatures controlled")
    void damageModeCountsControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Token mode creates two Mites that can't block")
    void tokenModeCreatesMitesThatCantBlock() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        List<Permanent> mites = findPermanents(player1, "Mite");
        assertThat(mites).hasSize(2);
        assertThat(mites).allSatisfy(mite -> assertThat(bls.canBlock(gd, mite)).isFalse());
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void damageModeCannotTargetPlayer() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
