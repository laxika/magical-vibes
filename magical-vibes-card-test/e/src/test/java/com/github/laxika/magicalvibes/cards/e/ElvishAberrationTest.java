package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Taiga;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishAberration.class, Forest.class, GrizzlyBears.class, Taiga.class})
class ElvishAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Elvish Aberration adds three green mana")
    void tappingAddsThreeGreenMana() {
        Permanent aberration = harness.addToBattlefieldAndReturn(player1, new ElvishAberration());
        aberration.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(aberration.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Aberration");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2).allMatch(card -> card instanceof Forest);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Forestcycling requires two generic mana and leaves the card in hand when unpaid")
    void forestcyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Elvish Aberration");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling can fail to find a Forest in an empty library")
    void forestcyclingCanFailToFindAForest() {
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Aberration");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents tapping Elvish Aberration for mana")
    void summoningSicknessPreventsManaAbility() {
        Permanent aberration = harness.addToBattlefieldAndReturn(player1, new ElvishAberration());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(aberration.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana ability resolves immediately and cannot tap an already tapped creature")
    void manaAbilityResolvesImmediatelyAndRequiresUntappedCreature() {
        Permanent aberration = harness.addToBattlefieldAndReturn(player1, new ElvishAberration());
        aberration.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(aberration.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forestcycling pays and discards immediately but searches only on resolution")
    void forestcyclingUsesTheStackAndAcceptsColoredMana() {
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Elvish Aberration");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling may decline to find a Forest even when one is available")
    void forestcyclingMayDeclineAnAvailableForest() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Elvish Aberration");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Forestcycling can find a nonbasic land with the Forest subtype")
    void forestcyclingFindsNonbasicForests() {
        Taiga taiga = new Taiga();
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of(new ElvishAberration(), taiga));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(taiga);
        harness.assertInGraveyard(player1, "Elvish Aberration");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("reveals Taiga")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
