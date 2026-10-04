package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreshMeat.class, PorcelainLegionnaire.class})
class FreshMeatTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 3/3 Beast token per creature that died this turn")
    void createsTokensPerCreatureDeath() {
        // Simulate 2 creature deaths for player 1 this turn
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 2, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        // Should have 2 Beast tokens
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(2);
        for (Permanent beast : beasts) {
            assertThat(beast.getCard().isToken()).isTrue();
            assertThat(beast.getCard().getPower()).isEqualTo(3);
            assertThat(beast.getCard().getToughness()).isEqualTo(3);
            assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        }
    }

    @Test
    @DisplayName("Creates no tokens if no creatures died this turn")
    void createsNoTokensIfNoDeaths() {
        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).isEmpty();
    }

    @Test
    @DisplayName("Only counts controller's creature deaths, not opponent's")
    void onlyCountsControllerDeaths() {
        // Opponent had 3 creatures die, controller had 1
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player2.getId(), 3, Integer::sum);
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
    }

    @Test
    @DisplayName("Works after an actual creature death from state-based actions")
    void worksAfterActualCreatureDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Porcelain Legionnaire");

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
    }

    @Test
    @DisplayName("Creates tokens for multiple deaths in same turn")
    void createsTokensForMultipleDeaths() {
        // Simulate 5 creatures dying (e.g. board wipe)
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 5, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(5);
    }

    @Test
    @DisplayName("Counts an owned creature that died under an opponent's control")
    void countsOwnedCreatureControlledByOpponent() {
        PorcelainLegionnaire card = new PorcelainLegionnaire();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Porcelain Legionnaire");

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not count an opponent-owned creature that died under your control")
    void excludesOpponentOwnedCreature() {
        PorcelainLegionnaire card = new PorcelainLegionnaire();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Porcelain Legionnaire");

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counts token deaths even though the tokens cease to exist")
    void countsTokenDeaths() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.setHand(player1, List.of(new FreshMeat(), new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castAndResolveInstant(player1, 0);

        Permanent beast = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(beast.getCard().isToken()).isTrue();
        beast.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.isToken());

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counts creatures that die after casting but before resolution")
    void countsDeathsBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0);

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Porcelain Legionnaire");
    }

    @Test
    @DisplayName("Does not count creature cards merely present in the graveyard")
    void ignoresCardsNotPutIntoGraveyardFromBattlefieldThisTurn() {
        harness.setGraveyard(player1, List.of(new PorcelainLegionnaire()));
        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
