package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElementalMascot.class, GrizzlyBears.class, Hurricane.class, Island.class, Shock.class})
class ElementalMascotTest extends BaseCardTest {

    private Permanent addMascot(Player player) {
        return addCreatureReady(player, new ElementalMascot());
    }

    private void setDeck(Player player, int islands) {
        harness.setLibrary(player, IntStream.range(0, islands).mapToObj(i -> new Island()).toList());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting a cheap instant gives +1/+0 and exiles nothing")
    void cheapSpellBoostsWithoutExile() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(mascot.getPowerModifier()).isEqualTo(1);
        assertThat(mascot.getToughnessModifier()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a five-mana spell gives +1/+0 and exiles the top card with play permission")
    void fiveManaSpellBoostsAndExiles() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player1);
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(mascot.getPowerModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mascot.getPowerModifier()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void fourManaSorceryBoostsWithoutExiling() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(mascot.getPowerModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void opponentsInstantDoesNotTrigger() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(mascot.getPowerModifier()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void multipleInstantCastsStackBoostsUntilEndOfTurn() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setDeck(player2, 5);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(mascot.getPowerModifier()).isEqualTo(2);
        assertThat(mascot.getToughnessModifier()).isZero();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(mascot.getPowerModifier()).isZero();
    }

    @Test
    void emptyLibraryStillAllowsBoost() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 0);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(mascot.getPowerModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void removingMascotInResponseDoesNotPreventExile() {
        Permanent mascot = addMascot(player1);
        setDeck(player1, 5);
        setUpMainPhase(player1);
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));

        harness.castSorcery(player1, 0, 4);
        harness.castInstant(player2, 0, mascot.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, mascot.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mascot);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void exiledLandCanBePlayedDuringNextTurn() {
        addMascot(player1);
        setDeck(player1, 5);
        setDeck(player2, 5);
        setUpMainPhase(player1);
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 4);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void unusedExilePermissionExpiresAfterNextTurn() {
        addMascot(player1);
        setDeck(player1, 5);
        setDeck(player2, 5);
        setUpMainPhase(player1);
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 4);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        addMascot(player1);
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Island()));
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castSorcery(player1, 0, 4);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }
}
