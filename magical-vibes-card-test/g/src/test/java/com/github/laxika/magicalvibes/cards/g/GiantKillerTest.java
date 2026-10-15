package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChopDown;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantKiller.class, ChopDown.class, CrawWurm.class, GrizzlyBears.class})
class GiantKillerTest extends BaseCardTest {

    @Test
    void adventureDestroysCreatureWithPowerFourOrGreaterAndExilesTheCard() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        GiantKiller card = new GiantKiller();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(wurm);
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetCreatureWithPowerLessThanFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GiantKiller card = new GiantKiller();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityTapsTargetCreature() {
        GiantKiller card = new GiantKiller();
        Permanent giantKiller = addCreatureReady(player1, card);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(giantKiller.isTapped()).isTrue();
    }

    @Test
    void creatureCanBeCastNormallyWithoutGoingOnAnAdventure() {
        harness.castFromHand(player1, new GiantKiller(), "{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Killer");
        harness.assertNotInGraveyard(player1, "Giant Killer");
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        GiantKiller card = new GiantKiller();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Killer");
        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureDestroysCreatureWithExactlyFourEffectivePower() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        wurm.setPersistentPowerModifier(-2);
        GiantKiller card = new GiantKiller();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureDoesNotResolveOrExileWhenTargetPowerDropsBelowFour() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        GiantKiller card = new GiantKiller();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, wurm.getId());

        wurm.setPersistentPowerModifier(-3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player1, "Giant Killer");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void summoningSickCreatureCannotPayTapCost() {
        Permanent giantKiller = harness.addToBattlefieldAndReturn(player1, new GiantKiller());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(giantKiller.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityCanTargetItsOwnSource() {
        Permanent giantKiller = addCreatureReady(player1, new GiantKiller());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, giantKiller.getId());
        assertThat(giantKiller.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(giantKiller.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityCannotBeActivatedAgainWhileSourceIsTapped() {
        Permanent giantKiller = addCreatureReady(player1, new GiantKiller());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(giantKiller.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }
}
