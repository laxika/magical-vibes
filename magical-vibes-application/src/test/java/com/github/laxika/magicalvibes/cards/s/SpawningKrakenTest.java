package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpawningKraken.class, GrizzlyBears.class})
class SpawningKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("A Kraken, Leviathan, Octopus, or Serpent dealing combat damage creates a 9/9 blue Kraken")
    void seaMonsterCombatDamageCreatesKrakenToken() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReadyWithSubtype(CardSubtype.KRAKEN);
        addCreatureReadyWithSubtype(CardSubtype.LEVIATHAN);
        addCreatureReadyWithSubtype(CardSubtype.OCTOPUS);
        addCreatureReadyWithSubtype(CardSubtype.SERPENT);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2, 3, 4, 5));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Kraken")).isEqualTo(5);
        assertThat(findPermanents(player1, "Kraken").stream()
                .allMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getPower() == 9
                        && permanent.getCard().getToughness() == 9
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getSubtypes().contains(CardSubtype.KRAKEN)))
                .isTrue();
    }

    @Test
    @DisplayName("A non-sea-monster dealing combat damage does not trigger Spawning Kraken")
    void nonSeaMonsterCombatDamageDoesNotCreateToken() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Kraken")).isZero();
    }

    private Permanent addCreatureReadyWithSubtype(CardSubtype subtype) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(subtype));
        return addCreatureReady(player1, card);
    }

    @Test
    @DisplayName("Each Spawning Kraken triggers for each attacking Kraken")
    void multipleCopiesTriggerForEachDamageDealer() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReady(player1, new SpawningKraken());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 8);
        assertThat(countPermanents(player1, "Kraken")).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage to a creature does not create a Kraken token")
    void blockedKrakenDoesNotCreateToken() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReady(player2, new SpawningKraken());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Spawning Kraken");
        harness.assertInGraveyard(player2, "Spawning Kraken");
        assertThat(countPermanents(player1, "Kraken")).isZero();
        assertThat(countPermanents(player2, "Kraken")).isZero();
    }

    @Test
    @DisplayName("Only the controller of the damaging Kraken creates a token")
    void opposingKrakenDoesNotTriggerYourKraken() {
        addCreatureReady(player1, new SpawningKraken()).setTapped(true);
        addCreatureReady(player2, new SpawningKraken());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        assertThat(countPermanents(player1, "Kraken")).isZero();
        assertThat(countPermanents(player2, "Kraken")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Kraken token created by the ability can trigger it in a later combat")
    void createdKrakenTokenCanTriggerAbility() {
        addCreatureReady(player1, new SpawningKraken());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Kraken");
        token.setSummoningSick(false);
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 5);
        assertThat(countPermanents(player1, "Kraken")).isEqualTo(2);
    }
}
