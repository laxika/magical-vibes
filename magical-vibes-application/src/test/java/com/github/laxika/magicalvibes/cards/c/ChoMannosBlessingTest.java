package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoMannosBlessing.class, FreshVolunteers.class, CreditVoucher.class})
class ChoMannosBlessingTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasProtectionFromChosenColorOnly() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(creature.getId());
        blessing.setChosenColor(CardColor.BLACK);
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    void noProtectionBeforeColorIsChosen() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, creature, color)).isFalse();
        }
    }

    @Test
    void protectionIsLostWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(creature.getId());
        blessing.setChosenColor(CardColor.RED);
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blessing);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    void choosingColorDoesNotRemoveTheAuraItself() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent blessing = new Permanent(new ChoMannosBlessing());
        blessing.setAttachedTo(creature.getId());
        blessing.setChosenColor(CardColor.WHITE);
        gd.playerBattlefields.get(player1.getId()).add(blessing);

        boolean changed = harness.getPermanentRemovalService().enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        harness.assertOnBattlefield(player1, "Cho-Manno's Blessing");
    }

    @Test
    void castingItChoosesColorAndGrantsProtection() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new ChoMannosBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
    }

    @Test
    void canBeCastDuringCombatBecauseItHasFlash() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new ChoMannosBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new CreditVoucher());
        harness.setHand(player1, List.of(new ChoMannosBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent artifact = findPermanent(player1, "Credit Voucher");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
