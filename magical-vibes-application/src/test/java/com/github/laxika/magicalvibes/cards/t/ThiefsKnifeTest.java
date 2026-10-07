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

    @Test
    @DisplayName("Equip moves all bonuses and each attached Knife grants its own draw trigger")
    void equipMovesBonusesAndDrawAbilitiesStack() {
        castKnife();
        Permanent firstKnife = findPermanent(player1, "Thief's Knife");
        Permanent firstHero = findPermanent(player1, "Hero");
        castKnife();
        Permanent secondHero = findPermanents(player1, "Hero").get(1);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstKnife), null, secondHero.getId());
        harness.passBothPriorities();

        assertThat(firstKnife.getAttachedTo()).isEqualTo(secondHero.getId());
        assertThat(gqs.getEffectivePower(gd, firstHero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstHero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, firstHero))
                .contains(CardSubtype.HERO).doesNotContain(CardSubtype.ROGUE);
        assertThat(gqs.getEffectivePower(gd, secondHero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondHero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, secondHero))
                .contains(CardSubtype.HERO, CardSubtype.ROGUE);

        firstHero.setSummoningSick(false);
        secondHero.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstHero),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondHero)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Job select still creates the Hero if the Knife leaves before the trigger resolves")
    void jobSelectCreatesHeroWithoutEquipment() {
        harness.setHand(player1, List.of(new ThiefsKnife()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent knife = findPermanent(player1, "Thief's Knife");
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, knife);
        resolveAllTriggers();

        Permanent hero = findPermanent(player1, "Hero");
        harness.assertNotOnBattlefield(player1, "Thief's Knife");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO).doesNotContain(CardSubtype.ROGUE);
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
