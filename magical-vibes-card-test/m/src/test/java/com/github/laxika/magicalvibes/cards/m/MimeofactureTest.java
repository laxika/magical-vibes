package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
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

@CardUsed({Mimeofacture.class, GhostWarden.class, Gristleback.class, HatchingPlans.class, WitnessProtection.class})
class MimeofactureTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards with the target permanent's name from that player's library")
    void offersCardsWithTargetPermanentName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden matching = new GhostWarden();
        harness.setLibrary(player2, List.of(matching, new Gristleback(), new HatchingPlans()));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst().getId()).isEqualTo(matching.getId());
    }

    @Test
    @DisplayName("Puts the chosen same-name card onto the caster's battlefield")
    void putsChosenCardUnderCasterControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden found = new GhostWarden();
        harness.setLibrary(player2, List.of(found));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(found.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Replicate creates an additional same-name search")
    void replicateCreatesAdditionalSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden first = new GhostWarden();
        GhostWarden second = new GhostWarden();
        harness.setLibrary(player2, List.of(first, second));

        castMimeofacture(target, List.of("{3}{U}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Replicate copies may choose a different permanent and matching card name")
    void replicateCopyMayTargetDifferentPermanent() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        GhostWarden foundGhostWarden = new GhostWarden();
        Gristleback foundGristleback = new Gristleback();
        harness.setLibrary(player2, List.of(foundGhostWarden, foundGristleback));

        castMimeofacture(originalTarget, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(foundGhostWarden.getId(), foundGristleback.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(originalTarget, copyTarget);
    }

    @Test
    @DisplayName("Shuffles without putting a card onto the battlefield when no name matches")
    void noMatchingCardLeavesLibraryAndBattlefieldUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        HatchingPlans otherCard = new HatchingPlans();
        Gristleback otherPermanentCard = new Gristleback();
        harness.setLibrary(player2, List.of(otherCard, otherPermanentCard));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(otherCard, otherPermanentCard);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        harness.setHand(player1, List.of(new Mimeofacture()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }


    @Test
    @DisplayName("May fail to find even when a same-name card is present")
    void mayDeclineMatchingCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden matching = new GhostWarden();
        harness.setLibrary(player2, List.of(matching));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(matching);
    }

    @Test
    @DisplayName("Can search for a noncreature permanent")
    void putsMatchingEnchantmentOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HatchingPlans());
        HatchingPlans matching = new HatchingPlans();
        harness.setLibrary(player2, List.of(matching));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(matching.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each replicate payment creates another independent search")
    void twoReplicatePaymentsCreateTwoCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden first = new GhostWarden();
        GhostWarden second = new GhostWarden();
        GhostWarden third = new GhostWarden();
        harness.setLibrary(player2, List.of(first, second, third));

        castMimeofacture(target, List.of("{3}{U}", "{3}{U}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.LibrarySearch.class);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Uses the target's current name after a name-changing effect")
    void renamedPermanentDoesNotMatchItsOriginalName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        GhostWarden originalNameCard = new GhostWarden();
        harness.setLibrary(player2, List.of(originalNameCard));
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        castMimeofacture(target, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(originalNameCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(originalNameCard);
    }

    private void castMimeofacture(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Mimeofacture()));
        harness.addMana(player1, ManaColor.BLUE, 4 + replicatePayments.size() * 4);
        harness.castSorceryWithRepeatedCosts(player1, 0, replicatePayments, List.of(target.getId()));
    }
}
