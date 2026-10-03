package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArniMetalbrow;
import com.github.laxika.magicalvibes.cards.l.LeylineImmersion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CampusRenovation.class, Forest.class, GrizzlyBears.class, AuraOfSilence.class, TormodsCrypt.class,
        ArniMetalbrow.class, LeylineImmersion.class})
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
        harness.castAndResolveSorcery(player1, 0, enchantment.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId()));

        Card optionalEnchantment = new AuraOfSilence();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(optionalEnchantment));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of());

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

    @Test
    @DisplayName("Cannot target an artifact in an opponent's graveyard")
    void cannotTargetOpponentsArtifact() {
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles nothing when its chosen target becomes illegal")
    void illegalTargetStopsEntireSpell() {
        Card artifact = new TormodsCrypt();
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castSorcery(player1, 0, artifact.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tormod's Crypt");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact)
                .doesNotContain(first, second);
        harness.assertInGraveyard(player1, "Campus Renovation");
    }

    @Test
    @DisplayName("Can resolve with an empty graveyard and play an exiled land using the normal land limit")
    void playsExiledLandWithoutGraveyardTarget() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiled spells still require their normal mana costs")
    void paysManaToCastExiledSpell() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Unplayed cards remain exiled after permission expires at the end of the caster's next turn")
    void expiresAtEndOfNextTurn() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of());

        int castTurn = gd.turnNumber;
        TurnCleanupService cleanup = GameTestEngineContext.get().getBean(TurnCleanupService.class);
        cleanup.applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());
        gd.turnNumber = castTurn + 1;
        cleanup.applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());
        gd.turnNumber = castTurn + 2;
        cleanup.applyCleanupResets(gd);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns an Aura attached to a chosen legal creature before exiling cards")
    void returnsAuraAttachedToChosenCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArniMetalbrow());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ArniMetalbrow());
        Card aura = new LeylineImmersion();
        Card top = new Forest();
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of(top));
        castCampusRenovation(aura.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.handlePermanentChosen(player1, chosen.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getAttachedTo()).isEqualTo(chosen.getId()).isNotEqualTo(first.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("An Aura with no legal attachment stays in the graveyard while the library cards are exiled")
    void auraStaysInGraveyardWithoutLegalAttachment() {
        Card aura = new LeylineImmersion();
        Card top = new Forest();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of(top));
        castCampusRenovation(aura.getId());

        harness.assertNotOnBattlefield(player1, "Leyline Immersion");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCampusRenovation(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CampusRenovation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
