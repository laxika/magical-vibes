package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IllGottenInheritance;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalPayment.class, SenateCourier.class, IllGottenInheritance.class})
class FinalPaymentTest extends BaseCardTest {

    @Test
    void paysLifeToDestroyTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Senate Courier");
    }

    @Test
    void sacrificesAnEnchantmentInsteadOfPayingLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new IllGottenInheritance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ill-Gotten Inheritance");
        harness.assertInGraveyard(player2, "Senate Courier");
    }

    @Test
    void sacrificesACreatureWhenLifeIsTooLowToPay() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertLife(player1, 4);
        harness.assertInGraveyard(player1, "Senate Courier");
        harness.assertOnBattlefield(player2, "Senate Courier");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Senate Courier");
    }

    @Test
    void cannotPayMoreLifeThanAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 4);
        harness.assertOnBattlefield(player2, "Senate Courier");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Senate Courier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetAnEnchantmentThatIsNotACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IllGottenInheritance());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Ill-Gotten Inheritance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSacrificeTheTargetToPayTheAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Senate Courier");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Final Payment");
        assertThat(gd.stack).isEmpty();
    }
}
