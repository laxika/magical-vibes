package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.k.KavuChameleon;
import com.github.laxika.magicalvibes.cards.l.LightningDart;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PledgeOfLoyalty.class, BenalishLancer.class, KavuAggressor.class, KavuChameleon.class,
        NomadicElf.class, Plains.class, LightningDart.class})
class PledgeOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from colors of permanents controlled by the Aura controller")
    void grantsProtectionFromAuraControllerColors() {
        Permanent enchanted = addCreatureReady(player2, new BenalishLancer());
        Permanent redPermanent = harness.addToBattlefieldAndReturn(player1, new KavuAggressor());
        harness.addToBattlefield(player2, new NomadicElf());
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.GREEN)).isFalse();

        Permanent pledge = findPermanent(player1, "Pledge of Loyalty");
        assertThat(pledge.isAttached()).isTrue();
        assertThat(pledge.getAttachedTo()).isEqualTo(enchanted.getId());

        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Uses a color-changing creature's layer-5 color without recursing through protection")
    void handlesColorChangedEnchantedPermanent() {
        Permanent enchanted = addCreatureReady(player1, new KavuChameleon());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, enchanted)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Granted protection prevents a red spell from targeting the enchanted creature")
    void preventsRedSpellTargeting() {
        Permanent enchanted = addCreatureReady(player1, new BenalishLancer());
        harness.addToBattlefield(player1, new KavuAggressor());
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningDart()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
