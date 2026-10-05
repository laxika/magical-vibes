package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FusionElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Malfegor.class, FusionElemental.class, ManaCylix.class})
class MalfegorTest extends BaseCardTest {

    private List<UUID> creatureIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fusion Elemental"))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(Player player) {
        return countPermanents(player, "Fusion Elemental");
    }

    private void addMalfegorMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("ETB discards controller's hand and each opponent sacrifices one creature per card")
    void discardsHandAndOpponentSacrificesOnePerCard() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new ManaCylix())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // One card left in hand after casting -> discarded; opponent's only creature auto-sacrificed.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mana Cylix");
        assertThat(creatureCount(player2)).isZero();
        harness.assertInGraveyard(player2, "Fusion Elemental");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with more creatures than cards discarded chooses which to sacrifice")
    void opponentChoosesWhenMoreCreaturesThanDiscards() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new ManaCylix())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, creatureIds(player2, 1));

        assertThat(creatureCount(player2)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice count scales with the number of cards discarded")
    void sacrificeCountScalesWithCardsDiscarded() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new ManaCylix(), new ManaCylix())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Two cards discarded, opponent has exactly two creatures -> both auto-sacrificed, no choice.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Empty hand causes no discard and no opponent sacrifice")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(creatureCount(player2)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller's own creatures are not sacrificed")
    void controllerCreaturesUnaffected() {
        harness.addToBattlefield(player1, new FusionElemental());
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, new ArrayList<>(List.of(new Malfegor(), new ManaCylix())));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent sacrifices all available creatures when fewer than cards discarded")
    void fewerCreaturesThanDiscards() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.addToBattlefield(player2, new ManaCylix());
        harness.setHand(player1, List.of(new Malfegor(), new ManaCylix(), new ManaCylix()));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Fusion Elemental");
        harness.assertOnBattlefield(player2, "Mana Cylix");
        harness.assertOnBattlefield(player1, "Malfegor");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Hand is discarded even when the opponent has no creatures")
    void discardsWithoutOpponentCreatures() {
        harness.addToBattlefield(player2, new ManaCylix());
        harness.setHand(player1, List.of(new Malfegor(), new ManaCylix()));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mana Cylix");
        harness.assertOnBattlefield(player2, "Mana Cylix");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discard count uses the hand when the triggered ability resolves")
    void usesHandAtTriggerResolution() {
        harness.addToBattlefield(player2, new FusionElemental());
        harness.addToBattlefield(player2, new FusionElemental());
        harness.setHand(player1, List.of(new Malfegor()));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ManaCylix(), new ManaCylix()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(creatureCount(player2)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent chooses two distinct creatures when two cards are discarded")
    void choosesMultipleCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FusionElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FusionElemental());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new FusionElemental());
        harness.addToBattlefield(player2, new ManaCylix());
        harness.setHand(player1, List.of(new Malfegor(), new ManaCylix(), new ManaCylix()));
        addMalfegorMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor).doesNotContain(first, second);
        assertThat(creatureCount(player2)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Mana Cylix");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
