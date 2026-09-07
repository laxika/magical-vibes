package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CampusRenovation.class, Forest.class, GrizzlyBears.class, AuraOfSilence.class, TormodsCrypt.class})
class CampusRenovationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact to the battlefield and exiles the top two cards for play")
    void returnsArtifactAndExilesTopCards() {
        Card artifact = new TormodsCrypt();
        Card topCard = new Forest();
        Card secondCard = new Forest();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castCampusRenovation(artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifact.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(topCard, secondCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Can return an enchantment and may choose no graveyard target")
    void returnsEnchantmentOrSkipsReturn() {
        Card enchantment = new AuraOfSilence();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castSorcery(player1, 0, enchantment.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId()));

        Card optionalEnchantment = new AuraOfSilence();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(optionalEnchantment));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(optionalEnchantment);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot target a creature card in a graveyard")
    void cannotTargetCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCampusRenovation(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castSorcery(player1, 0, targetId, List.of());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
