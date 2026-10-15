package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.b.BoundInGold;
import com.github.laxika.magicalvibes.cards.b.BountyAgent;
import com.github.laxika.magicalvibes.cards.b.BringToTrial;
import com.github.laxika.magicalvibes.cards.c.CollarTheCulprit;
import com.github.laxika.magicalvibes.cards.c.CompulsoryRest;
import com.github.laxika.magicalvibes.cards.e.Expel;
import com.github.laxika.magicalvibes.cards.f.FairgroundsWarden;
import com.github.laxika.magicalvibes.cards.g.GlassCasket;
import com.github.laxika.magicalvibes.cards.i.IronVerdict;
import com.github.laxika.magicalvibes.cards.l.LuminousBonds;
import com.github.laxika.magicalvibes.cards.o.Outflank;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.Reprobation;
import com.github.laxika.magicalvibes.cards.s.SealAway;
import com.github.laxika.magicalvibes.cards.s.SummaryJudgment;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlayersBounty.class, BountyAgent.class, Outflank.class, BoundInGold.class,
        BringToTrial.class, GlassCasket.class, Reprobation.class, CollarTheCulprit.class,
        CompulsoryRest.class, Expel.class, FairgroundsWarden.class, IronVerdict.class,
        LuminousBonds.class, RaiseTheAlarm.class, SealAway.class, SummaryJudgment.class,
        ThrabenInspector.class, Shock.class, AllIsDust.class})
class SlayersBountyTest extends BaseCardTest {

    @Test
    void entersLookingAtTargetOpponentsCreatureCardsOnly() {
        harness.setHand(player2, List.of(new BountyAgent(), new Shock()));
        harness.setHand(player1, List.of(new SlayersBounty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.clearMessages();

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(harness.getConn1().getSentMessages().stream()
                .filter(message -> message.contains("REVEAL_HAND")).toList())
                .anyMatch(message -> message.contains("Bounty Agent"))
                .noneMatch(message -> message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages().stream()
                .filter(message -> message.contains("REVEAL_HAND")).toList())
                .noneMatch(message -> message.contains("Bounty Agent"));
    }

    @Test
    void sacrificingSlayersBountyDrawsAndDrafts() {
        Card drawCard = new Shock();
        harness.setLibrary(player1, List.of(drawCard));
        harness.addToBattlefield(player1, new SlayersBounty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        harness.assertInGraveyard(player1, "Slayer's Bounty");

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void sacrificingAnotherClueDraftsWithoutSacrificingSlayersBounty() {
        harness.addToBattlefield(player1, new SlayersBounty());
        harness.enterBattlefieldAndReturn(player1, new ThrabenInspector());
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
        harness.assertOnBattlefield(player1, "Slayer's Bounty");
    }

    @Test
    void sacrificingBountyToAllIsDustDraftsExactlyOnce() {
        harness.addToBattlefield(player1, new SlayersBounty());
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Slayer's Bounty");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
    }

    @Test
    void opponentsClueSacrificeDoesNotDraftForYourBounty() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new SlayersBounty());
        harness.addToBattlefield(player2, new SlayersBounty());
        Card drawCard = new Shock();
        harness.setLibrary(player2, List.of(drawCard));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player2, List.of(drafted.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(drawCard, drafted);
        harness.assertOnBattlefield(player1, "Slayer's Bounty");
    }

    @Test
    void destroyingBountyDoesNotDraft() {
        Permanent bounty = harness.addToBattlefieldAndReturn(player1, new SlayersBounty());
        addCreatureReady(player2, new BountyAgent());

        harness.activateAbility(player2, 0, null, bounty.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Slayer's Bounty");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void lookingAtHandWithoutCreaturesDoesNotExposeOtherCards() {
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new SlayersBounty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.clearMessages();

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(harness.getConn1().getSentMessages().stream()
                .filter(message -> message.contains("REVEAL_HAND")).toList())
                .noneMatch(message -> message.contains("Shock"));
        harness.assertOnBattlefield(player1, "Slayer's Bounty");
        harness.assertInHand(player2, "Shock");
    }
}
