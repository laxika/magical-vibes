package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeathbloomThallid;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetHulk;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetSprout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FungalPlots;
import com.github.laxika.magicalvibes.cards.h.HauntedMire;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RhizomeLurcher;
import com.github.laxika.magicalvibes.cards.t.ThallidOmnivore;
import com.github.laxika.magicalvibes.cards.t.ThallidSoothsayer;
import com.github.laxika.magicalvibes.cards.v.VerdantEmbrace;
import com.github.laxika.magicalvibes.cards.v.VerdantForce;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.cards.y.YavimayaCradleOfGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlimefootThallidTransplant.class, Forest.class, Island.class, Swamp.class, HauntedMire.class,
        DeathbloomThallid.class, DeathbonnetSprout.class, DeathbonnetHulk.class,
        RhizomeLurcher.class, SporeCrawler.class, SporecrownThallid.class,
        Sporemound.class, SwarmShambler.class, ThallidOmnivore.class,
        ThallidSoothsayer.class, YavimayaSapherd.class, FungalPlots.class,
        VerdantForce.class, VerdantEmbrace.class, SporeSwarm.class,
        SaprolingMigration.class})
class SlimefootThallidTransplantTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Deathbloom Thallid", "Deathbonnet Sprout", "Rhizome Lurcher", "Spore Crawler",
            "Sporecrown Thallid", "Sporemound", "Swarm Shambler", "Thallid Omnivore",
            "Thallid Soothsayer", "Yavimaya Sapherd", "Fungal Plots", "Verdant Force",
            "Verdant Embrace", "Spore Swarm", "Saproling Migration");

    @Test
    void forestTriggersThreeCardDraftAndPutsChosenCardIntoHand() {
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice choice =
                gd.interaction.activeInteraction(
                        PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3).doesNotHaveDuplicates();
        assertThat(choice.cards()).allMatch(card -> !card.isTokenCard())
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);

        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly(chosen.getName());
    }

    @Test
    void nonForestNonSwampLandDoesNotTriggerDraft() {
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class)).isNull();
    }

    @Test
    void swampEnteringWithoutBeingPlayedTriggersDraft() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.enterBattlefieldAndReturn(player1, new Swamp());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class)).isNotNull();
    }

    @Test
    void landWithBothSwampAndForestTypesTriggersOnlyOnce() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.enterBattlefieldAndReturn(player1, new HauntedMire());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        var choice = gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class)).isNull();
    }

    @Test
    void opposingForestDoesNotTriggerDraft() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class)).isNull();
    }

    @Test
    void draftStillResolvesAfterSlimefootLeavesBattlefield() {
        harness.setHand(player1, List.of());
        var slimefoot = harness.addToBattlefieldAndReturn(player1, new SlimefootThallidTransplant());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(slimefoot);
        gd.playerGraveyards.get(player1.getId()).add(slimefoot.getCard());

        harness.passBothPriorities();
        var choice = gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
    }

    @Test
    @CardUsed(YavimayaCradleOfGrowth.class)
    void islandEnteringAsForestUnderYavimayaTriggersDraft() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlimefootThallidTransplant());
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        harness.enterBattlefieldAndReturn(player1, new Island());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice.class)).isNotNull();
    }
}
