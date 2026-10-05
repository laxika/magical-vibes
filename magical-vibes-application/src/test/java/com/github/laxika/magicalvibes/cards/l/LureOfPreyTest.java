package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LureOfPrey.class, MtendaLion.class, BayFalcon.class, DarkRitual.class, Dissipate.class})
class LureOfPreyTest extends BaseCardTest {

    @Test
    void canRespondToUnresolvedOpponentCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mtenda Lion");
        harness.assertNotOnBattlefield(player2, "Mtenda Lion");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Mtenda Lion");
    }

    @Test
    void resolvesWithNoCardsRemainingInHand() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new LureOfPrey(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lure of Prey");
    }

    @Test
    void greenNoncreatureCardCannotBePutOntoBattlefield() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new LureOfPrey()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsOnlyOneCreatureOntoBattlefield() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Mtenda Lion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("After an opponent casts a creature spell, puts a green creature from hand onto the battlefield")
    void putsGreenCreatureAfterOpponentCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mtenda Lion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Not castable when no opponent cast a creature spell this turn")
    void notCastableWithoutOpponentCreatureSpell() {
        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Your own creature spell does not enable the cast")
    void ownCreatureSpellDoesNotEnableCast() {
        harness.setHand(player1, List.of(new MtendaLion(), new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Declining the may leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mtenda Lion");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only green creature cards are eligible to be put onto the battlefield")
    void onlyGreenCreaturesAreEligible() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MtendaLion(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new BayFalcon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Bay Falcon");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not enable the cast")
    void nonCreatureSpellDoesNotEnableCast() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A countered opponent creature spell still enables the cast")
    void counteredOpponentCreatureSpellStillEnablesCast() {
        MtendaLion creatureSpell = new MtendaLion();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, creatureSpell, "{G}");
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Dissipate(), new LureOfPrey(), new MtendaLion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creatureSpell.getId());

        harness.assertNotOnBattlefield(player2, "Mtenda Lion");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Mtenda Lion");

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mtenda Lion");
    }
}
