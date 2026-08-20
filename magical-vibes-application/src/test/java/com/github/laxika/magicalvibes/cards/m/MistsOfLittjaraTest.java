package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castMistsOfLittjara(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mists of Littjara can enchant a noncreature Vehicle")
    void canEnchantVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());

        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, schooner.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mists of Littjara")
                        && schooner.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Mists of Littjara cannot enchant another noncreature permanent")
    void cannotEnchantOtherPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("The weakening ends when Mists of Littjara leaves the battlefield")
    void weakeningEndsWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castMistsOfLittjara(bears);

        Permanent aura = findPermanent(player1, "Mists of Littjara");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void castMistsOfLittjara(Permanent target) {
        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
