package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExoticCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1 for each distinct basic land type you control")
    void appliesDomainPenalty() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new AvatarOfMight());
        Permanent avatar = findPermanent(player2, "Avatar of Might");

        harness.setHand(player1, List.of(new ExoticCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(5);
    }

    @Test
    @DisplayName("Duplicate basic lands count only once and opponent lands do not count")
    void countsDistinctTypesControlledByAuraController() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new AvatarOfMight());
        Permanent avatar = findPermanent(player2, "Avatar of Might");

        harness.setHand(player1, List.of(new ExoticCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(7);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new ExoticCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent forest = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
