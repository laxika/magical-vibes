package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnassumingSage.class})
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
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void sorcererRoleScriesWhenSageAttacks() {
        castSage(2);
        harness.handleMayAbilityChosen(player1, true);
        Permanent sage = findPermanent(player1, "Unassuming Sage");
        sage.setSummoningSick(false);
        UnassumingSage top = new UnassumingSage();
        UnassumingSage next = new UnassumingSage();
        harness.setLibrary(player1, List.of(top, next));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
    }

    @Test
    void roleCanEnchantSageAfterOpponentGainsControl() {
        castSage(2);
        Permanent sage = findPermanent(player1, "Unassuming Sage");
        gd.playerBattlefields.get(player1.getId()).remove(sage);
        gd.playerBattlefields.get(player2.getId()).add(sage);

        harness.handleMayAbilityChosen(player1, true);

        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(role.getAttachedTo()).isEqualTo(sage.getId());
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
    }

    @Test
    void noRoleIsCreatedIfSageLeavesBeforePayment() {
        castSage(2);
        Permanent sage = findPermanent(player1, "Unassuming Sage");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, sage));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
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
        resolveAllTriggers();
    }
}
