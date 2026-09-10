package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabletOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills a card and grants permission to play it this turn")
    void entersMillsAndGrantsPlayPermission() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setHand(player1, List.of(new TabletOfDiscovery()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.graveyardPlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("The first ability adds one red mana")
    void firstAbilityAddsOneRedMana() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Tablet of Discovery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability adds two red mana restricted to instant and sorcery spells")
    void secondAbilityAddsRestrictedRedMana() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The restricted ability cannot cast a creature spell")
    void secondAbilityCannotCastCreature() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{R}");
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
