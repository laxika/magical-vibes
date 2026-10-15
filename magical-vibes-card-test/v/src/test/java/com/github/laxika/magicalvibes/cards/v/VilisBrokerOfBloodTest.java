package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.k.KasminasTransmutation;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VilisBrokerOfBlood.class, Forest.class, GreenwoodSentinel.class, Shock.class,
        KasminasTransmutation.class})
class VilisBrokerOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life draws two cards and gives a creature -1/-1")
    void activatedAbilityPaysLifeDrawsAndShrinks() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage life loss draws that many cards")
    void damageCausesLifeLossTrigger() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The life-payment draw trigger resolves before the shrink ability")
    void paymentTriggerResolvesAboveActivatedAbility() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay two life with only one life remaining")
    void cannotActivateWithInsufficientLife() {
        Permanent vilis = harness.addToBattlefieldAndReturn(player1, new VilisBrokerOfBlood());
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vilis.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent losing life does not draw cards for Vilis's controller")
    void opponentLifeLossDoesNotTrigger() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations draw for each payment and can kill a creature")
    void repeatedActivationsDrawAndReduceToughnessToZero() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, sentinel.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Losing the target does not undo the life payment or the draw trigger")
    void drawsEvenWhenActivatedAbilityTargetDies() {
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        harness.castInstant(player1, 0, sentinel.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vilis can target itself and the shrink expires at end of turn")
    void canTargetItselfAndShrinkExpires() {
        Permanent vilis = harness.addToBattlefieldAndReturn(player1, new VilisBrokerOfBlood());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, vilis.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vilis)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, vilis)).isEqualTo(7);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, vilis)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, vilis)).isEqualTo(8);
    }

    @Test
    @DisplayName("Vilis does not trigger on life loss after losing all abilities")
    @CardUsed({VilisBrokerOfBlood.class, KasminasTransmutation.class, Shock.class, Forest.class})
    void losingAllAbilitiesStopsLifeLossTrigger() {
        Permanent vilis = harness.addToBattlefieldAndReturn(player1, new VilisBrokerOfBlood());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KasminasTransmutation(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, vilis.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
