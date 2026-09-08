package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinalPaymentTest extends BaseCardTest {

    @Test
    void paysLifeToDestroyTargetCreature() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sacrificesAnEnchantmentInsteadOfPayingLife() {
        Permanent sacrifice = new Permanent(new FurnaceOfRath());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace of Rath");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
