package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.t.TemptWithTreats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GumdropPoisoner.class, TemptWithTreats.class, AirElemental.class})
class GumdropPoisonerTest extends BaseCardTest {

    @Test
    void adventureCreatesFoodAndExilesTheCard() {
        GumdropPoisoner card = new GumdropPoisoner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceGetsMinusThreeMinusThreeAfterGainingThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        GumdropPoisoner card = new GumdropPoisoner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId(), target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Gumdrop Poisoner");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void creatureFaceMayEnterWithoutChoosingATarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        GumdropPoisoner card = new GumdropPoisoner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void enterAbilityCanTargetOnlyCreatures() {
        GumdropPoisoner card = new GumdropPoisoner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeGainedInResponseToEnterTriggerCounts() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GumdropPoisoner());
        GumdropPoisoner card = new GumdropPoisoner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFromExile(player1, card.getId(), target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player2, "Gumdrop Poisoner");
        harness.assertNotOnBattlefield(player2, "Gumdrop Poisoner");
    }

    @Test
    void cumulativeLifeGainedCountsWithoutSubtractingLifeLost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test setup");
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
        harness.setHand(player1, List.of(new GumdropPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void opponentsLifeGainDoesNotIncreaseTheReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GumdropPoisoner());
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);
        harness.setHand(player1, List.of(new GumdropPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Gumdrop Poisoner");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }
}
