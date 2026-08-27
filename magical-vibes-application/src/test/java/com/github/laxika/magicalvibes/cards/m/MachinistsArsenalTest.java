package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MachinistsArsenal.class, GrizzlyBears.class, LeoninScimitar.class})
class MachinistsArsenalTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates a Hero token and attaches Machinist's Arsenal to it")
    void jobSelectCreatesAndEquipsHero() {
        castArsenal();

        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(arsenal.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(hero.getCard().getPower()).isEqualTo(1);
        assertThat(hero.getCard().getToughness()).isEqualTo(1);
        assertThat(hero.getCard().getSubtypes()).contains(CardSubtype.HERO);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.ARTIFICER);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 for each artifact controlled by the Equipment's controller")
    void boostScalesWithControlledArtifacts() {
        castArsenal();
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);

        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip moves Machinist's Arsenal to another creature")
    void equipMovesArsenal() {
        castArsenal();
        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(arsenal.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.ARTIFICER);
    }

    private void castArsenal() {
        harness.setHand(player1, List.of(new MachinistsArsenal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
