package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiftOfOrzhovaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gift of Orzhova attaches it to the target creature")
    void resolvingAttachesToTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GiftOfOrzhova()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gift of Orzhova")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has flying and lifelink")
    void enchantedCreatureGetsBoostAndKeywords() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        Permanent gift = new Permanent(new GiftOfOrzhova());
        gift.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and keywords when Gift of Orzhova leaves")
    void effectsStopWhenRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        Permanent gift = new Permanent(new GiftOfOrzhova());
        gift.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);

        gd.playerBattlefields.get(player1.getId()).remove(gift);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Other creatures are unaffected")
    void doesNotAffectOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        Permanent otherBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent gift = new Permanent(new GiftOfOrzhova());
        gift.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfOrzhova()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
