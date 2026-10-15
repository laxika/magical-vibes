package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonksFist.class, GrizzlyBears.class})
class MonksFistTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Monk's Fist creates and equips a Hero token")
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new MonksFist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent fist = findPermanent(player1, "Monk's Fist");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(fist.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO, CardSubtype.MONK);
    }

    @Test
    @DisplayName("Equip {2} moves Monk's Fist and its bonus to another creature")
    void equipMovesFist() {
        Permanent fist = addCreatureReady(player1, new MonksFist());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        fist.setAttachedTo(first.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(fist.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.MONK);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.MONK);
    }

    @Test
    @DisplayName("Job select still creates a Hero if the Equipment leaves before resolution")
    void createsHeroWhenEquipmentLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new MonksFist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent fist = findPermanent(player1, "Monk's Fist");
        assertThat(countPermanents(player1, "Hero")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(fist);
        gd.playerGraveyards.get(player1.getId()).add(fist.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO)
                .doesNotContain(CardSubtype.MONK);
    }

    @Test
    @DisplayName("Each Monk's Fist attaches to its own newly created Hero")
    void multipleCopiesAttachToTheirOwnHeroes() {
        harness.setHand(player1, List.of(new MonksFist(), new MonksFist()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstFist = findPermanent(player1, "Monk's Fist");
        Permanent firstHero = findPermanent(player1, "Hero");

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(2);
        Permanent secondFist = findPermanents(player1, "Monk's Fist").get(1);
        Permanent secondHero = findPermanents(player1, "Hero").get(1);
        assertThat(firstFist.getAttachedTo()).isEqualTo(firstHero.getId());
        assertThat(secondFist.getAttachedTo()).isEqualTo(secondHero.getId());
        for (Permanent hero : findPermanents(player1, "Hero")) {
            assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
            assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO, CardSubtype.MONK);
        }
    }
}
