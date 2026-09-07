package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodrageMycoid.class, ZuranOrb.class, Forest.class})
class BroodrageMycoidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fungus token that can't block at your end step after descending")
    void createsNonBlockingFungusAfterDescending() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> fungus = findPermanents(player1, "Fungus");
        assertThat(fungus).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FUNGUS);
            assertThat(bls.canBlock(gd, token)).isFalse();
        });
    }

    @Test
    @DisplayName("Does not create a Fungus token at your end step without descending")
    void doesNotCreateFungusWithoutDescending() {
        harness.addToBattlefield(player1, new BroodrageMycoid());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
