package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinRally;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetStaticaster;
import com.github.laxika.magicalvibes.cards.l.LaunchParty;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpicExperiment.class, Forest.class, GiantGrowth.class, GrizzlyBears.class,
        Shock.class, Cancel.class, GoblinRally.class, IzzetStaticaster.class, LaunchParty.class, RuleOfLaw.class})
class EpicExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards of the library")
    void exilesTopXCards() {
        Forest a = new Forest();
        Forest b = new Forest();
        Forest c = new Forest();
        Forest leftover = new Forest();
        cast(2, List.of(a, b, c, leftover));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c, leftover);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(a, b);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Offers only exiled instants/sorceries with mana value X or less")
    void onlyOffersInstantsAndSorceriesAtOrBelowX() {
        Shock shock = new Shock();                 // instant, MV 1
        GiantGrowth growth = new GiantGrowth();    // instant, MV 1
        GrizzlyBears bears = new GrizzlyBears();   // creature — not castable
        Forest forest = new Forest();              // land — not castable

        cast(3, List.of(shock, growth, bears, forest));

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(shock.getId(), growth.getId());
        assertThat(interaction.validCardIds()).doesNotContain(bears.getId(), forest.getId());
    }

    @Test
    @DisplayName("Instant/sorcery with mana value greater than X is not offered")
    void excludesSpellsAboveX() {
        // Cancel is {1}{U}{U} = MV 3; X=2 should not offer it.
        Cancel cancel = new Cancel();
        Shock shock = new Shock();

        cast(2, List.of(cancel, shock));

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(shock.getId());
        assertThat(interaction.validCardIds()).doesNotContain(cancel.getId());
    }

    @Test
    @DisplayName("Choosing an exiled instant casts it without paying; unchosen cards go to the graveyard")
    void castsChosenAndGraveyardsRemainder() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(2, List.of(shock, forest));

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Declining every cast puts all exiled cards into the graveyard")
    void decliningPutsAllIntoGraveyard() {
        Shock shock = new Shock();
        Forest forest = new Forest();

        cast(2, List.of(shock, forest));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("X zero leaves the library untouched")
    void zeroDoesNothing() {
        Forest forest = new Forest();
        cast(0, List.of(forest));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library shorter than X exiles only the available cards")
    void shorterLibrary() {
        Forest forest = new Forest();
        cast(5, List.of(forest));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An eligible spell with no legal targets goes to the graveyard")
    void noLegalTargets() {
        GiantGrowth growth = new GiantGrowth();
        cast(1, List.of(growth));
        harness.handleMultipleCardsChosen(player1, List.of(growth.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(growth);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A sorcery at the mana-value limit is cast and resolves for free")
    void castsSorceryAtLimit() {
        GoblinRally rally = new GoblinRally();
        cast(5, List.of(rally));
        harness.handleMultipleCardsChosen(player1, List.of(rally.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(rally);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rally);
    }

    @Test
    @DisplayName("Multiple spells are cast in the chosen order and their X is zero")
    void castsMultipleXSpells() {
        EpicExperiment first = new EpicExperiment();
        EpicExperiment second = new EpicExperiment();
        Forest remaining = new Forest();
        cast(2, List.of(first, second, remaining));
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(second, first);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A payable additional sacrifice cost does not prevent casting")
    void permitsPayableAdditionalCost() {
        LaunchParty launchParty = new LaunchParty();
        harness.addToBattlefield(player1, new IzzetStaticaster());
        harness.addToBattlefield(player2, new IzzetStaticaster());
        cast(4, List.of(launchParty));
        harness.handleMultipleCardsChosen(player1, List.of(launchParty.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(launchParty);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Rule of Law prevents casting spells after Epic Experiment")
    void respectsSpellLimit() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        EpicExperiment exiled = new EpicExperiment();
        Forest forest = new Forest();
        cast(2, List.of(exiled, forest));
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiled, forest);
        assertThat(gd.exiledCards).isEmpty();
    }

    private void cast(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EpicExperiment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
