package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThiefsKnife.class, Forest.class})
class ThiefsKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates a Hero token and attaches Thief's Knife to it")
    void jobSelectCreatesAndEquipsHero() {
        castKnife();

        Permanent knife = findPermanent(player1, "Thief's Knife");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(knife.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("The equipped creature draws a card after dealing combat damage to a player")
    void equippedCreatureDrawsOnCombatDamage() {
        castKnife();
        Permanent hero = findPermanent(player1, "Hero");
        hero.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        int heroIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hero);
        declareAttackers(List.of(heroIndex));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castKnife() {
        harness.setHand(player1, List.of(new ThiefsKnife()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
