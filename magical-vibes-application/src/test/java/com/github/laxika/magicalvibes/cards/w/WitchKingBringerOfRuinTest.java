package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchKingBringerOfRuin.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class WitchKingBringerOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the defending player sacrifice their least-power creature")
    void sacrificesDefendingPlayersLeastPowerCreature() {
        addCreatureReady(player1, new WitchKingBringerOfRuin());
        Permanent lowerPowerAttacker = addCreatureReady(player1, new LlanowarElves());
        Permanent least = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lowerPowerAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(least);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The defending player chooses among creatures tied for least power")
    void defendingPlayerChoosesTiedLeastPowerCreature() {
        addCreatureReady(player1, new WitchKingBringerOfRuin());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(bears.getId(), spider.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does nothing when the defending player controls no creatures")
    void doesNothingWithoutDefendingCreatures() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingBringerOfRuin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(witchKing);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
