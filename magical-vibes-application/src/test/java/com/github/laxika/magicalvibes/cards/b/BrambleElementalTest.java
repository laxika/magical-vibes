package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleElemental.class, HolyStrength.class})
class BrambleElementalTest extends BaseCardTest {

    @Test
    void auraAttachedToBrambleElementalCreatesTwoSaprolings() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachHolyStrength(player1, elemental);

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    void opponentAuraAttachedToBrambleElementalCreatesTokensForElementalsController() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachHolyStrength(player2, elemental);

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    private void attachHolyStrength(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new HolyStrength()));
        harness.addMana(controller, ManaColor.WHITE, 1);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
