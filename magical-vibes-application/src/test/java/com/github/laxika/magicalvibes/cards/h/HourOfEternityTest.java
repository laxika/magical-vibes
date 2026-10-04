package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SunscourgeChampion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({HourOfEternity.class, HarrierNaga.class, Plains.class, BramblewoodParagon.class,
        FrilledSandwalla.class, SunscourgeChampion.class})
class HourOfEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 prompts for X target creature cards from your graveyard")
    void castingPromptsGraveyardChoice() {
        Card naga1 = new HarrierNaga();
        Card naga2 = new HarrierNaga();
        Card plains = new Plains();
        harness.setGraveyard(player1, List.of(naga1, naga2, plains));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7); // {X}{X}{U}{U}{U} with X=2 costs 4 generic + UUU

        harness.castSorcery(player1, 0, 2);

        // Awaiting the X-scaled graveyard choice; spell not yet on the stack
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        // Only the controller's creature cards are valid (Plains excluded)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(naga1.getId(), naga2.getId());
        assertThat(gd.stack).isEmpty();
    }

    /**
     * The copied Paragons are Zombies, not Warriors, and therefore receive no Warrior counters.
     */
    @Test
    @DisplayName("Zombie copies of Paragons do not give each other Warrior counters")
    void zombieParagonCopiesDoNotPumpEachOther() {
        Card paragon1 = new BramblewoodParagon();
        Card paragon2 = new BramblewoodParagon();
        harness.setGraveyard(player1, List.of(paragon1, paragon2));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(paragon1.getId(), paragon2.getId()));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Resolving exiles the creatures and creates a 4/4 black Zombie copy of each")
    void resolvingCreatesFourFourBlackZombieCopies() {
        Card naga1 = new HarrierNaga();
        Card naga2 = new HarrierNaga();
        harness.setGraveyard(player1, List.of(naga1, naga2));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(naga1.getId(), naga2.getId()));
        harness.passBothPriorities();

        // Both creatures exiled (only Hour of Eternity itself remains in the graveyard)
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Hour of Eternity");

        // Two token copies of Harrier Naga on the battlefield (proves they are copies, not generic Zombies)
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Harrier Naga");
            assertThat(token.getCard().getManaCost()).isEqualTo("{2}{G}");
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(4);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            // The copy replaces the original creature types with Zombie.
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        }
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution creates fewer copies")
    void targetRemovedBeforeResolutionCreatesFewerCopies() {
        Card naga1 = new HarrierNaga();
        Card naga2 = new HarrierNaga();
        harness.setGraveyard(player1, List.of(naga1, naga2));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(naga1.getId(), naga2.getId()));

        // One target leaves the graveyard before resolution
        gd.playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(naga1.getId()));

        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting with X greater than the number of creature cards in the graveyard is illegal")
    void xGreaterThanCreatureCountThrows() {
        harness.setGraveyard(player1, List.of(new HarrierNaga())); // only one creature card
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }

    @Test
    @DisplayName("X=0 resolves without targets or tokens")
    void zeroResolvesWithoutTargets() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hour of Eternity");
    }

    @Test
    @DisplayName("All targets leaving the graveyard produces no tokens")
    void allTargetsRemovedCreatesNoCopies() {
        Card naga = new HarrierNaga();
        harness.setGraveyard(player1, List.of(naga));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(naga.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(naga));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(naga);
        harness.assertInGraveyard(player1, "Hour of Eternity");
    }

    @Test
    @DisplayName("Opponent's graveyard cannot supply the required targets")
    void opponentCreaturesCannotBeTargeted() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new HarrierNaga()));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }

    @Test
    @DisplayName("Token copies retain the creature's activated ability")
    void copyRetainsActivatedAbility() {
        Card sandwalla = new FrilledSandwalla();
        harness.setGraveyard(player1, List.of(sandwalla));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(sandwalla.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(6);
        assertThat(token.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Copied enters ability uses the token's overridden power")
    void copiedEntersAbilityUsesFourPower() {
        Card champion = new SunscourgeChampion();
        harness.setGraveyard(player1, List.of(champion));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(champion.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("An existing Paragon does not give the Zombie copy a Warrior counter")
    void zombieCopyDoesNotReceiveWarriorCounter() {
        harness.addToBattlefield(player1, new BramblewoodParagon());
        Card naga = new HarrierNaga();
        harness.setGraveyard(player1, List.of(naga));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(naga.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
    }
}
