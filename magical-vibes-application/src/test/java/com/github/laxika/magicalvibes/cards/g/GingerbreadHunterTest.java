package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.p.PunySnack;
import com.github.laxika.magicalvibes.cards.r.RedtoothVanguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GingerbreadHunter.class, PunySnack.class, BeanstalkWurm.class, RedtoothVanguard.class})
class GingerbreadHunterTest extends BaseCardTest {

    @Test
    void adventureWeakensTargetCreatureUntilEndOfTurnAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BeanstalkWurm());
        GingerbreadHunter card = new GingerbreadHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void adventureCanTargetOnlyCreatures() {
        GingerbreadHunter card = new GingerbreadHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCreatesFoodOnEntryAndCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BeanstalkWurm());
        GingerbreadHunter card = new GingerbreadHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gingerbread Hunter");
        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void foodCreatedByCreatureFaceCanBeSacrificedForThreeLife() {
        GingerbreadHunter card = new GingerbreadHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void adventureCanKillYourOwnCreatureAndStillExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        GingerbreadHunter card = new GingerbreadHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
        harness.assertInGraveyard(player1, "Redtooth Vanguard");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void adventureWithAnIllegalTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RedtoothVanguard());
        GingerbreadHunter first = new GingerbreadHunter();
        GingerbreadHunter second = new GingerbreadHunter();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAdventure(player1, 0, target.getId());
        harness.castAdventure(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Redtooth Vanguard");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(second.getId())).isEqualTo(player1.getId());
    }

    @Test
    void foodIsSacrificedAsACostAndLifeIsGainedOnlyOnResolution() {
        harness.setHand(player1, List.of(new GingerbreadHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        Permanent food = findPermanent(player1, "Food");
        assertThat(food.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    void foodRequiresTwoManaAndAnUntappedToken() {
        harness.setHand(player1, List.of(new GingerbreadHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        assertThat(food.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        food.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
