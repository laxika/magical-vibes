package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castFromHand(player1, new MachinistsArsenal(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void opponentsArtifactsAndControlledNonartifactsDoNotIncreaseBoost() {
        castArsenal();
        Permanent hero = findPermanent(player1, "Hero");
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.ARTIFICER);
    }

    @Test
    void jobSelectStillCreatesHeroWhenEquipmentLeavesBeforeTriggerResolves() {
        harness.castFromHand(player1, new MachinistsArsenal(), "{4}{W}");
        harness.passBothPriorities();
        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        gd.playerBattlefields.get(player1.getId()).remove(arsenal);
        gd.playerGraveyards.get(player1.getId()).add(arsenal.getCard());

        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO).doesNotContain(CardSubtype.ARTIFICER);
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        castArsenal();
        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arsenal.getAttachedTo()).isEqualTo(hero.getId());
    }

    @Test
    void equipRequiresFourMana() {
        castArsenal();
        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arsenal.getAttachedTo()).isEqualTo(hero.getId());
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        castArsenal();
        Permanent arsenal = findPermanent(player1, "Machinist's Arsenal");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arsenal.getAttachedTo()).isEqualTo(hero.getId());
    }
}
