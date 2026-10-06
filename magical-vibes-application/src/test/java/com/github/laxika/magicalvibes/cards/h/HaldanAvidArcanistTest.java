package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PakoArcaneRetriever;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpatialContortion;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaldanAvidArcanist.class, PakoArcaneRetriever.class, Forest.class,
        GrizzlyBears.class, Shock.class, SpatialContortion.class, TurnToFrog.class})
class HaldanAvidArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Pako")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card pako = new PakoArcaneRetriever();
        harness.setLibrary(player2, List.of(pako));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(pako);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plays fetch-counter lands and casts fetch-counter noncreature spells with any mana")
    void playsLandsAndCastsNoncreatureSpellsFromFetchCounters() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card land = new Forest();
        Card spell = new Shock();
        Card creature = new GrizzlyBears();
        gd.addToExileWithFetchCounter(player2.getId(), land, player1.getId());
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        gd.addToExileWithFetchCounter(player2.getId(), creature, player1.getId());

        prepareMainPhase();
        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant permission for cards without fetch counters or exiled by another player")
    void restrictsPermissionToFetchCardsExiledByController() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card unmarked = new Shock();
        Card opponentExiled = new Shock();
        gd.addToExile(player1.getId(), unmarked);
        gd.addToExileWithFetchCounter(player2.getId(), opponentExiled, player2.getId());

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, unmarked.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, opponentExiled.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The partner search may fail to find an available Pako")
    void partnerSearchMayFailToFind() {
        Card pako = new PakoArcaneRetriever();
        harness.setLibrary(player2, List.of(pako));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(pako);
    }

    @Test
    @DisplayName("Fetch-counter permission disappears when Haldan leaves and returns with a new Haldan")
    void permissionRequiresHaldanOnBattlefield() {
        Card spell = new Shock();
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        var haldan = harness.addToBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, haldan);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fetch-counter lands cannot be played during another player's turn")
    void landsRetainNormalTiming() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card land = new Forest();
        gd.addToExileWithFetchCounter(player2.getId(), land, player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
    }

    @Test
    @DisplayName("Fetched instants may be cast during another player's turn")
    void instantsRetainInstantTiming() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card spell = new Shock();
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }
    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void partnerSearchMayBeDeclined() {
        Card pako = new PakoArcaneRetriever();
        harness.setLibrary(player2, List.of(pako));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(pako);
    }

    @Test
    @DisplayName("Fetch-counter spells still require payment")
    void fetchCounterSpellsAreNotFree() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card spell = new Shock();
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Fetch-counter lands consume the normal land play")
    void fetchCounterLandsRespectLandPlayLimit() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card first = new Forest();
        Card second = new Forest();
        gd.addToExileWithFetchCounter(player2.getId(), first, player1.getId());
        gd.addToExileWithFetchCounter(player2.getId(), second, player1.getId());
        prepareMainPhase();
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    @DisplayName("Losing all abilities removes Haldan's fetch-counter permission")
    void losingAbilitiesRemovesFetchPermission() {
        var haldan = harness.addToBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        Card spell = new Shock();
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        harness.setHand(player1, List.of(new TurnToFrog()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, haldan.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Any-color permission does not pay an explicit colorless mana cost")
    void coloredManaCannotPayColorlessRequirement() {
        var haldan = harness.addToBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        Card spell = new SpatialContortion();
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), haldan.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }
}
