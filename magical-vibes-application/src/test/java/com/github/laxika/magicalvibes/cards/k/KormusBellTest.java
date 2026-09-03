package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KormusBell.class, BadMoon.class, EvilPresence.class, Forest.class, Swamp.class})
class KormusBellTest extends BaseCardTest {

    @Test
    @DisplayName("Swamps of both players become 1/1 creatures that are still lands")
    void animatesSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent swamp1 = findPermanent(player1, "Swamp");
        assertThat(gqs.isCreature(gd, swamp1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp1)).isEqualTo(1);
        assertThat(gqs.isLand(gd, swamp1)).isTrue();

        Permanent swamp2 = findPermanent(player2, "Swamp");
        assertThat(gqs.isCreature(gd, swamp2)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp2)).isEqualTo(1);
        assertThat(gqs.isLand(gd, swamp2)).isTrue();
    }

    @Test
    @DisplayName("Non-Swamp lands are unaffected")
    void doesNotAnimateNonSwampLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("Animated Swamps are black creatures, so Bad Moon pumps them to 2/2")
    void animatedSwampsAreBlackCreaturesForBadMoon() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());
        harness.addToBattlefield(player1, new BadMoon());

        Permanent swamp = findPermanent(player1, "Swamp");
        assertThat(gqs.getEffectiveColors(gd, swamp)).containsExactly(CardColor.BLACK);
        // The Swamp is only a creature because of Kormus Bell; being a black creature, Bad Moon's
        // +1/+1 applies: 1/1 (Kormus Bell) + 1/1 (Bad Moon) = 2/2.
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(2);
    }

    @Test
    @DisplayName("A land that becomes a Swamp is animated")
    void animatesLandWithSwampSubtypeGrantedByAnotherEffect() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent evilPresence = harness.addToBattlefieldAndReturn(player1, new EvilPresence());
        evilPresence.setAttachedTo(forest.getId());
        harness.addToBattlefield(player1, new KormusBell());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Swamps revert to non-creatures when Kormus Bell leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent swamp = findPermanent(player1, "Swamp");
        assertThat(gqs.isCreature(gd, swamp)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kormus Bell"));

        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(0);
    }
}
