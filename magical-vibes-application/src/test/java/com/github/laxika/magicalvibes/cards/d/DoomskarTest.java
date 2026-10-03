package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.i.InSearchOfGreatness;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.t.ToskiBearerOfSecrets;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Doomskar.class, FearlessPup.class, InSearchOfGreatness.class, RaidersKarve.class, ToskiBearerOfSecrets.class})
class DoomskarTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures but not other permanents")
    void destroysAllCreaturesButNotOtherPermanents() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.addToBattlefield(player1, new InSearchOfGreatness());

        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Fearless Pup");
        harness.assertInGraveyard(player2, "Fearless Pup");
        harness.assertOnBattlefield(player1, "In Search of Greatness");
        harness.assertInGraveyard(player1, "Doomskar");
    }

    @Test
    @DisplayName("Foretell exiles Doomskar face down and allows casting it on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        Doomskar doomskar = new Doomskar();
        harness.setHand(player1, List.of(doomskar));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(doomskar.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.exiledTurnNumber()).isEqualTo(gd.turnNumber);
        assertThat(gd.foretoldCardIds).contains(doomskar.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.castFromExile(player1, doomskar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, doomskar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doomskar");
        harness.assertInGraveyard(player1, "Fearless Pup");
        harness.assertInGraveyard(player2, "Fearless Pup");
        harness.assertOnBattlefield(player1, "In Search of Greatness");
        assertThat(gd.foretoldCardIds).doesNotContain(doomskar.getId());
    }

    @Test
    @DisplayName("Indestructible creatures and uncrewed Vehicles survive the destruction")
    void sparesIndestructibleCreaturesAndUncrewedVehicles() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new ToskiBearerOfSecrets());
        harness.addToBattlefield(player2, new RaidersKarve());
        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Fearless Pup");
        harness.assertNotOnBattlefield(player1, "Fearless Pup");
        harness.assertOnBattlefield(player2, "Toski, Bearer of Secrets");
        harness.assertOnBattlefield(player2, "Raiders' Karve");
        harness.assertInGraveyard(player1, "Doomskar");
    }

    @Test
    @DisplayName("Doomskar resolves with no creatures on the battlefield")
    void resolvesWithoutCreatures() {
        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Doomskar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foretelling requires two mana and leaves the card in hand when payment is impossible")
    void cannotForetellWithOnlyOneMana() {
        Doomskar doomskar = new Doomskar();
        harness.setHand(player1, List.of(doomskar));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doomskar);
        assertThat(gd.findExiledCard(doomskar.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("Foretelling is allowed during your upkeep but not during another player's turn")
    void foretellUsesOwnTurnPriorityTiming() {
        Doomskar doomskar = new Doomskar();
        harness.setHand(player1, List.of(doomskar));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doomskar);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(doomskar.getId()).faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a foretold Doomskar still requires sorcery timing and two white mana")
    void foretoldCastRequiresSorceryTimingAndWhiteMana() {
        Doomskar doomskar = new Doomskar();
        harness.setHand(player1, List.of(doomskar));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, doomskar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, doomskar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.findExiledCard(doomskar.getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, doomskar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doomskar");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }
}
