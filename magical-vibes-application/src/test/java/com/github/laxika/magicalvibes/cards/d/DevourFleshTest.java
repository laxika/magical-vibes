package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevourFleshTest extends BaseCardTest {

    @Test
    void targetPlayerSacrificesCreatureAndGainsLifeEqualToToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void targetPlayerChoosesCreatureWhenTheyControlMultiple() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        int lifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, spider.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(spider);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
