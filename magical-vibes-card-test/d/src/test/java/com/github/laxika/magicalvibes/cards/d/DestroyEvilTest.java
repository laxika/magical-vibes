package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestroyEvil.class, AirElemental.class, GrizzlyBears.class, GreaterAuramancy.class, FountainOfYouth.class})
class DestroyEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode destroys a creature with toughness 4 or greater")
    void destroysToughCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castDestroyEvil(0, target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Creature mode rejects a creature with toughness less than 4")
    void rejectsLowToughnessCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");
    }

    @Test
    @DisplayName("Enchantment mode destroys a target enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        harness.addToBattlefield(player2, new FountainOfYouth());
        castDestroyEvil(1, target);

        harness.assertNotOnBattlefield(player2, "Greater Auramancy");
        harness.assertInGraveyard(player2, "Greater Auramancy");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Enchantment mode rejects a non-enchantment permanent")
    void rejectsNonEnchantmentPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new GreaterAuramancy());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    @Test
    @DisplayName("Creature mode uses toughness including counters")
    void destroysCreatureWithIncreasedToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castDestroyEvil(0, target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature mode rechecks toughness when resolving")
    void doesNotDestroyCreatureWhoseToughnessDropsBelowFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCard();
        harness.castInstant(player1, 0, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Destroy Evil");
    }

    @Test
    @DisplayName("Damage marked on a creature does not reduce its toughness")
    void destroysDamagedToughCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setMarkedDamage(3);

        castDestroyEvil(0, target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Enchantment mode can destroy an enchantment you control")
    void destroysOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());

        castDestroyEvil(1, target);

        harness.assertNotOnBattlefield(player1, "Greater Auramancy");
        harness.assertInGraveyard(player1, "Greater Auramancy");
    }

    @Test
    @DisplayName("Enchantment mode fails if its target gains shroud before resolution")
    void doesNotDestroyEnchantmentThatGainsShroud() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        prepareCard();
        harness.castInstant(player1, 0, 1, target.getId());

        harness.addToBattlefield(player2, new GreaterAuramancy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target).hasSize(2);
        harness.assertNotInGraveyard(player2, "Greater Auramancy");
        harness.assertInGraveyard(player1, "Destroy Evil");
    }

    private void castDestroyEvil(int mode, Permanent target) {
        prepareCard();
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new DestroyEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
