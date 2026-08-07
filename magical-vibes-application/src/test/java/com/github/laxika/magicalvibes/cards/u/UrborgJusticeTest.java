package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrborgJusticeTest extends BaseCardTest {

    private void castUrborgJustice() {
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent sacrifices one creature per creature that died under the caster's control")
    void sacrificesOnePerControllerDeath() {
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 2, Integer::sum);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which creatures to sacrifice when they control more than died")
    void opponentChoosesWhenMoreCreaturesThanDeaths() {
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castUrborgJustice();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Giant Spider"))
                .findFirst()
                .orElseThrow();
        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nothing is sacrificed when no creature died under the caster's control")
    void noSacrificeWithoutDeaths() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castUrborgJustice();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Urborg Justice");
    }
}
