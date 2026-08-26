package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuramancersGuise.class, FountainOfYouth.class, GrizzlyBears.class,
        HolyStrength.class, LeoninScimitar.class})
class AuramancersGuiseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Auramancer's Guise attaches it and counts itself")
    void resolvesAndCountsItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AuramancersGuise()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent guise = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AuramancersGuise)
                .findFirst()
                .orElseThrow();
        assertThat(guise.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Auramancer's Guise updates for attached Auras but not Equipment")
    void updatesForAttachedAuras() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent guise = harness.addToBattlefieldAndReturn(player1, new AuramancersGuise());
        guise.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        Permanent strength = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        strength.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);

        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);

        gd.playerBattlefields.get(player1.getId()).remove(strength);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Auramancer's Guise cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AuramancersGuise()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
