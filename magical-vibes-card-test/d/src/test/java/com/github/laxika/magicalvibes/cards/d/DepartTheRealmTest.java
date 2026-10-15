package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.InSearchOfGreatness;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DepartTheRealm.class, InSearchOfGreatness.class, Island.class, Mistwalker.class})
class DepartTheRealmTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        harness.addToBattlefield(player2, new InSearchOfGreatness());
        UUID targetId = harness.getPermanentId(player2, "In Search of Greatness");
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "In Search of Greatness");
        harness.assertInHand(player2, "In Search of Greatness");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void foretellsAndCastsOnLaterTurn() {
        harness.addToBattlefield(player2, new Mistwalker());
        DepartTheRealm departTheRealm = new DepartTheRealm();
        harness.setHand(player1, List.of(departTheRealm));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(departTheRealm.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, departTheRealm.getId(),
                harness.getPermanentId(player2, "Mistwalker"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistwalker");
        harness.assertInHand(player2, "Mistwalker");
    }

    @Test
    void castsForetoldInstantDuringOpponentsNextTurn() {
        harness.addToBattlefield(player2, new Mistwalker());
        DepartTheRealm spell = new DepartTheRealm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, spell.getId(),
                harness.getPermanentId(player2, "Mistwalker"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mistwalker");
        harness.assertInHand(player2, "Mistwalker");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertInGraveyard(player1, "Depart the Realm");
    }

    @Test
    void cannotCastForetoldCardOnTheSameTurn() {
        harness.addToBattlefield(player2, new Mistwalker());
        DepartTheRealm spell = new DepartTheRealm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(),
                harness.getPermanentId(player2, "Mistwalker")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertOnBattlefield(player2, "Mistwalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        DepartTheRealm spell = new DepartTheRealm();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");

        harness.assertInHand(player2, "Depart the Realm");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void returnsControlledPermanentToItsOwnerRatherThanItsController() {
        Mistwalker creature = new Mistwalker();
        creature.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mistwalker");
        harness.assertInHand(player1, "Mistwalker");
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }
}
