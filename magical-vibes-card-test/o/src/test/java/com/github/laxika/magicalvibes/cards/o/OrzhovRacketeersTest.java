package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.cards.k.KayasWrath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrzhovRacketeers.class, KayasWrath.class, GrotesqueDemise.class})
class OrzhovRacketeersTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard a card")
    void combatDamageMakesDamagedPlayerDiscard() {
        Permanent racketeers = addCreatureReady(player1, new OrzhovRacketeers());
        racketeers.setAttacking(true);
        harness.setHand(player2, List.of(new OrzhovRacketeers(), new KayasWrath()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Afterlife 2 creates two white and black Spirit tokens with flying")
    void afterlifeCreatesTwoSpiritTokens() {
        harness.addToBattlefield(player1, new OrzhovRacketeers());
        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Orzhov Racketeers");

        List<Permanent> tokens = findPermanents(player1, "Spirit");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("An empty hand does not prevent the combat damage trigger from resolving")
    void combatDamageAgainstEmptyHand() {
        Permanent racketeers = addCreatureReady(player1, new OrzhovRacketeers());
        racketeers.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to the first player makes that player choose the discard")
    void opponentControlledRacketeersMakesFirstPlayerDiscard() {
        Permanent racketeers = addCreatureReady(player2, new OrzhovRacketeers());
        racketeers.setAttacking(true);
        OrzhovRacketeers keptCard = new OrzhovRacketeers();
        KayasWrath discardedCard = new KayasWrath();
        harness.setHand(player1, List.of(keptCard, discardedCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
    }

    @Test
    @DisplayName("Simultaneous deaths create two Spirits for each Racketeers' controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new OrzhovRacketeers());
        harness.addToBattlefield(player2, new OrzhovRacketeers());

        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Orzhov Racketeers");
        harness.assertInGraveyard(player2, "Orzhov Racketeers");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not cause a discard")
    void blockedCombatDoesNotCauseDiscard() {
        addCreatureReady(player1, new OrzhovRacketeers());
        addCreatureReady(player2, new OrzhovRacketeers());
        KayasWrath handCard = new KayasWrath();
        harness.setHand(player2, List.of(handCard));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        harness.assertInGraveyard(player1, "Orzhov Racketeers");
        harness.assertInGraveyard(player2, "Orzhov Racketeers");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiling Racketeers does not trigger afterlife")
    void exileDoesNotCreateSpirits() {
        Permanent racketeers = addCreatureReady(player2, new OrzhovRacketeers());
        harness.setHand(player1, List.of(new GrotesqueDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, racketeers.getId());
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(racketeers.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
