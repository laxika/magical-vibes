package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KormusBell.class, BadMoon.class, EvilPresence.class, Forest.class, Swamp.class, PhantasmalTerrain.class})
class KormusBellTest extends BaseCardTest {

    @Test
    @DisplayName("Swamps of both players become 1/1 creatures that are still lands")
    void animatesSwamps() {
        Permanent swamp1 = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent swamp2 = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        assertThat(gqs.isCreature(gd, swamp1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp1)).isEqualTo(1);
        assertThat(gqs.isLand(gd, swamp1)).isTrue();

        assertThat(gqs.isCreature(gd, swamp2)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp2)).isEqualTo(1);
        assertThat(gqs.isLand(gd, swamp2)).isTrue();
    }

    @Test
    @DisplayName("Non-Swamp lands are unaffected")
    void doesNotAnimateNonSwampLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new KormusBell());

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("Animated Swamps are black creatures, so Bad Moon pumps them to 2/2")
    void animatedSwampsAreBlackCreaturesForBadMoon() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());
        harness.addToBattlefield(player1, new BadMoon());

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
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        assertThat(gqs.isCreature(gd, swamp)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kormus Bell"));

        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(0);
    }

    @Test
    @DisplayName("A Swamp changed into an Island is no longer animated")
    void stopsAnimatingLandThatLosesSwampSubtype() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(swamp.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);

        assertThat(gqs.effectiveBasicLandTypes(gd, swamp)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.isLand(gd, swamp)).isTrue();
        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, swamp)).isEmpty();
    }

    @Test
    @DisplayName("A land changed into a Swamp after Kormus Bell enters is black and receives Bad Moon's bonus")
    void animatesNewlyGrantedSwampBeforeColorAndCreatureBonuses() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new KormusBell());
        harness.addToBattlefield(player1, new BadMoon());
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new EvilPresence());
        presence.setAttachedTo(forest.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Swamps entering after Kormus Bell are also animated")
    void animatesSwampsEnteringLater() {
        harness.addToBattlefield(player1, new KormusBell());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        assertThat(gqs.isCreature(gd, swamp)).isTrue();
        assertThat(gqs.isLand(gd, swamp)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, swamp)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(1);
    }
}
