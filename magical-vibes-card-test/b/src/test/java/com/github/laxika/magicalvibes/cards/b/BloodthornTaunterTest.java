package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodthornTaunter.class, AvatarOfMight.class, AirElemental.class,
        FountainOfYouth.class, WoollyThoctar.class})
class BloodthornTaunterTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to a target creature with power 5 or greater")
    void grantsHasteToBigCreature() {
        addCreatureReady(player1, new BloodthornTaunter());
        Permanent target = addCreatureReady(player2, new AvatarOfMight());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetSmallCreature() {
        addCreatureReady(player1, new BloodthornTaunter());
        Permanent target = addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 5 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new BloodthornTaunter());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateOnEntryAndGrantHasteToOwnCreatureWithExactlyFivePower() {
        Permanent taunter = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(taunter.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void targetBecomingTooSmallBeforeResolutionDoesNotGainHaste() {
        addCreatureReady(player1, new BloodthornTaunter());
        Permanent target = addCreatureReady(player2, new WoollyThoctar());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesEffectivePowerWhenChoosingTarget() {
        addCreatureReady(player1, new BloodthornTaunter());
        Permanent target = addCreatureReady(player2, new WoollyThoctar());
        target.setPowerModifier(-1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 5 or greater");

        target.setPowerModifier(1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        target.setPowerModifier(-1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }
}
