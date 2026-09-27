package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightOfSanction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopyEnchantment.class, LightOfSanction.class, CarvenCaryatid.class, Char.class})
class CopyEnchantmentTest extends BaseCardTest {

    @Test
    @DisplayName("May enter as a copy of an enchantment on the battlefield")
    void copiesAnEnchantment() {
        Permanent light = harness.addToBattlefieldAndReturn(player2, new LightOfSanction());
        Permanent creature = addCreatureReady(player1, new CarvenCaryatid());
        CopyEnchantment copy = new CopyEnchantment();
        castCopyEnchantment(copy);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, light.getId());

        castCharAt(creature);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Declining to copy leaves the enchantment unchanged")
    void declinesToCopy() {
        harness.addToBattlefield(player2, new LightOfSanction());
        Permanent creature = addCreatureReady(player1, new CarvenCaryatid());
        CopyEnchantment copy = new CopyEnchantment();
        castCopyEnchantment(copy);

        harness.handleMayAbilityChosen(player1, false);

        castCharAt(creature);

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Enters normally when no enchantment is on the battlefield")
    void entersNormallyWhenNoEnchantmentIsOnTheBattlefield() {
        harness.addToBattlefield(player2, new CarvenCaryatid());
        CopyEnchantment copy = new CopyEnchantment();

        castCopyEnchantment(copy);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findCopy(copy)).isNotNull();
    }

    private void castCharAt(Permanent target) {
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castCopyEnchantment(CopyEnchantment copy) {
        harness.castFromHand(player1, copy, "{2}{U}");
        harness.passBothPriorities();
    }

    private Permanent findCopy(CopyEnchantment copy) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(copy.getId()))
                .findFirst()
                .orElseThrow();
    }
}
