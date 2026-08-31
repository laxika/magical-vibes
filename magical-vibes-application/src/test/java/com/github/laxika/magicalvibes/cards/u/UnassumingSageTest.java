package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnassumingSage.class)
class UnassumingSageTest extends BaseCardTest {

    @Test
    void payingTwoManaCreatesSorcererRoleAttachedToIt() {
        castSage(3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent sage = findPermanent(player1, "Unassuming Sage");
        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(sage.getId());
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
    }

    @Test
    void decliningPaymentDoesNotCreateSorcererRole() {
        castSage(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
        assertThat(findPermanents(player1, "Unassuming Sage")).hasSize(1);
    }

    private void castSage(int remainingColorlessMana) {
        harness.setHand(player1, List.of(new UnassumingSage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, remainingColorlessMana + 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
