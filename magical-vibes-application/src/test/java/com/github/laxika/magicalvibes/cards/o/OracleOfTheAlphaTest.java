package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AncestralRecall;
import com.github.laxika.magicalvibes.cards.b.BlackLotus;
import com.github.laxika.magicalvibes.cards.m.MoxEmerald;
import com.github.laxika.magicalvibes.cards.m.MoxJet;
import com.github.laxika.magicalvibes.cards.m.MoxPearl;
import com.github.laxika.magicalvibes.cards.m.MoxRuby;
import com.github.laxika.magicalvibes.cards.m.MoxSapphire;
import com.github.laxika.magicalvibes.cards.t.TimeWalk;
import com.github.laxika.magicalvibes.cards.t.Timetwister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OracleOfTheAlpha.class, BlackLotus.class, MoxPearl.class, MoxSapphire.class,
        MoxJet.class, MoxRuby.class, MoxEmerald.class, AncestralRecall.class, TimeWalk.class,
        Timetwister.class})
class OracleOfTheAlphaTest extends BaseCardTest {

    @Test
    void enteringConjuresThePowerNineIntoTheLibrary() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new OracleOfTheAlpha());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(9)
                .extracting(Card::getName)
                .containsExactlyInAnyOrder(
                        "Black Lotus", "Mox Pearl", "Mox Sapphire", "Mox Jet", "Mox Ruby",
                        "Mox Emerald", "Ancestral Recall", "Time Walk", "Timetwister");
    }

    @Test
    void attackingScriesOne() {
        Card topCard = new AncestralRecall();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new OracleOfTheAlpha());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
