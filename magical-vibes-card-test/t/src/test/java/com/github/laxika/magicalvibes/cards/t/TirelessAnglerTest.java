package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Archipelagore;
import com.github.laxika.magicalvibes.cards.f.FleetSwallower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MoatPiranhas;
import com.github.laxika.magicalvibes.cards.m.MysticSkyfish;
import com.github.laxika.magicalvibes.cards.n.NadirKraken;
import com.github.laxika.magicalvibes.cards.p.PouncingShoreshark;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.cards.r.RuinCrab;
import com.github.laxika.magicalvibes.cards.s.SeaDasherOctopus;
import com.github.laxika.magicalvibes.cards.s.SerpentOfYawningDepths;
import com.github.laxika.magicalvibes.cards.s.SigiledStarfish;
import com.github.laxika.magicalvibes.cards.s.SpinedMegalodon;
import com.github.laxika.magicalvibes.cards.s.StingingLionfish;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.cards.v.VoraciousGreatshark;
import com.github.laxika.magicalvibes.cards.w.WormholeSerpent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessAngler.class, Island.class, FleetSwallower.class, MoatPiranhas.class, MysticSkyfish.class,
        NadirKraken.class, PouncingShoreshark.class, SeaDasherOctopus.class, SpinedMegalodon.class,
        StingingLionfish.class, VoraciousGreatshark.class, Archipelagore.class, SerpentOfYawningDepths.class,
        WormholeSerpent.class,
        SigiledStarfish.class, RiptideTurtle.class, RuinCrab.class, Swamp.class, Forest.class, UrborgTombOfYawgmoth.class})
class TirelessAnglerTest extends BaseCardTest {

    @Test
    void islandEnteringTriggersSpellbookDraft() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void swampEnteringTriggersSpellbookDraft() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
    }

    @Test
    void otherLandsDoNotTriggerSpellbookDraft() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void forestEnteringAsSwampTriggersSpellbookDraft() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.addToBattlefield(player2, new UrborgTombOfYawgmoth());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
    }

    @Test
    void urborgEnteringTriggersSpellbookDraftForItsOwnSwampType() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.setHand(player1, List.of(new UrborgTombOfYawgmoth()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
    }

    @Test
    void landThatIsBothIslandAndSwampTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void opponentsIslandDoesNotTriggerSpellbookDraft() {
        harness.addToBattlefield(player1, new TirelessAngler());

        harness.enterBattlefieldAndReturn(player2, new Island());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void eachAnglerTriggersIndependentlyForAnIsland() {
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.addToBattlefield(player1, new TirelessAngler());
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
            assertThat(choice).isNotNull();
            harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
