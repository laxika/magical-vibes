package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quasiduplicate.class, BartizanBats.class})
class QuasiduplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target creature you control")
    void createsTokenCopyOfTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Bartizan Bats");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Bartizan Bats"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Jump-start discards a card, creates a token copy, and exiles Quasiduplicate")
    void jumpStartDiscardsCreatesTokenCopyAndExilesSpell() {
        Quasiduplicate spell = new Quasiduplicate();
        Quasiduplicate discarded = new Quasiduplicate();
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Bartizan Bats");
        harness.castJumpStart(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Bartizan Bats"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new BartizanBats());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Bartizan Bats");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void jumpStartRequiresDiscard() {
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setGraveyard(player1, List.of(new Quasiduplicate()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Bartizan Bats");
        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Quasiduplicate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jump-start still requires the normal mana cost")
    void jumpStartRequiresManaCost() {
        Quasiduplicate discarded = new Quasiduplicate();
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setGraveyard(player1, List.of(new Quasiduplicate()));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Bartizan Bats");
        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jump-start exiles the spell even when its target becomes illegal")
    void jumpStartExilesWhenTargetBecomesIllegal() {
        Quasiduplicate spell = new Quasiduplicate();
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Bartizan Bats");
        harness.castJumpStart(player1, 0, 0, targetId);
        var target = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Can copy a token without copying its tapped state")
    void copiesTokenWithoutTappedState() {
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setHand(player1, List.of(new Quasiduplicate(), new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID originalId = harness.getPermanentId(player1, "Bartizan Bats");
        harness.castAndResolveSorcery(player1, 0, originalId);
        var firstToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        firstToken.tap();
        harness.castAndResolveSorcery(player1, 0, firstToken.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Bartizan Bats"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && !p.getId().equals(firstToken.getId()))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isFalse());
        harness.assertInGraveyard(player1, "Quasiduplicate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
