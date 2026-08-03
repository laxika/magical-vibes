package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DyingWishTest extends BaseCardTest {

    @Test
    void enchantedCreatureDeathTargetsPlayerAndUsesLastKnownPower() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent dyingWish = new Permanent(new DyingWish());
        dyingWish.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(dyingWish);

        int player1Before = gd.getLife(player1.getId());
        int player2Before = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        // The death trigger is queued as a pending target choice; it only becomes the active
        // interaction when a player would next receive priority.
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Before - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Before + 2);
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        // A creature the caster controls must exist, or the aura is unplayable before targeting is
        // ever validated (CR 601.2c) and the cast fails with the generic message.
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DyingWish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
