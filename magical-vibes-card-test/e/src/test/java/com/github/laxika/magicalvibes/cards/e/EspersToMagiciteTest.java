package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DivineVisitation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EspersToMagicite.class, GrizzlyBears.class, Plains.class, Shock.class, DivineVisitation.class, SoulWarden.class})
class EspersToMagiciteTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent graveyard and copies a chosen creature as an artifact")
    void exilesOpponentsGraveyardsAndCopiesChosenCreatureAsArtifact() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setGraveyard(player2, List.of(bears, shock));

        castEspersToMagicite();

        PendingInteraction.EspersToMagiciteCreatureChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EspersToMagiciteCreatureChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears, shock);
    }

    @Test
    @DisplayName("May decline the optional creature copy")
    void mayDeclineCreatureCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castEspersToMagicite();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chooses the reflexive target before players can respond to the ability")
    void choosesTargetBeforeReflexiveAbilityResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.castFromHand(player1, new EspersToMagicite(), "{3}{B}");

        harness.passBothPriorities();

        PendingInteraction.EspersToMagiciteCreatureChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EspersToMagiciteCreatureChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Artifact copies are not replaced by Divine Visitation")
    void artifactCopyDoesNotUseCreatureTokenReplacement() {
        harness.addToBattlefield(player1, new DivineVisitation());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castEspersToMagicite();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isFalse();
    }

    @Test
    @DisplayName("Exiles noncreature cards without creating a token")
    void noncreatureGraveyardCreatesNoToken() {
        Shock shock = new Shock();
        Plains plains = new Plains();
        harness.setGraveyard(player2, List.of(shock, plains));

        castEspersToMagicite();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock, plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not copy creatures in your own graveyard or previously exiled cards")
    void emptyOpponentGraveyardDoesNotCopyOtherCards() {
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears previouslyExiledBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears));
        harness.setExile(player2, List.of(previouslyExiledBears));

        castEspersToMagicite();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(previouslyExiledBears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Artifact copies retain the copied card's triggered abilities")
    void artifactCopyRetainsTriggeredAbility() {
        SoulWarden warden = new SoulWarden();
        harness.setGraveyard(player2, List.of(warden));
        harness.setLife(player1, 20);

        castEspersToMagicite();
        harness.handleMultipleCardsChosen(player1, List.of(warden.getId()));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Soul Warden");
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private void castEspersToMagicite() {
        harness.castFromHand(player1, new EspersToMagicite(), "{3}{B}");
        resolveAllTriggers();
    }
}
