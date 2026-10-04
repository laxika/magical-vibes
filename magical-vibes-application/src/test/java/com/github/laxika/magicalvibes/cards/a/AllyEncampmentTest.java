package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AllyEncampment.class)
class AllyEncampmentTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AllyEncampment());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability produces mana restricted to Ally spells")
    void tapsForRestrictedAnyColorMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AllyEncampment());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(land.isTapped()).isTrue();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.ALLY), ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast an Ally spell")
    void restrictedManaCastsAllySpell() {
        harness.addToBattlefieldAndReturn(player1, new AllyEncampment());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(createCreature("Ally Spell", CardSubtype.ALLY)));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast another creature spell")
    void restrictedManaCannotCastNonAllySpell() {
        harness.addToBattlefieldAndReturn(player1, new AllyEncampment());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(createCreature("Non-Ally Spell", CardSubtype.GOBLIN)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability sacrifices the land and returns a controlled Ally to its owner's hand")
    void sacrificesAndReturnsAlly() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AllyEncampment());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, createCreature("Ally to Return", CardSubtype.ALLY));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, ally.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ally Encampment");
        harness.assertNotOnBattlefield(player1, "Ally to Return");
        harness.assertInHand(player1, "Ally to Return");
        harness.assertInGraveyard(player1, "Ally Encampment");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability only targets an Ally you control")
    void bounceRequiresControlledAlly() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AllyEncampment());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1,
                createCreature("Non-Ally", CardSubtype.GOBLIN));
        Permanent opponentAlly = harness.addToBattlefieldAndReturn(player2,
                createCreature("Opponent Ally", CardSubtype.ALLY));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, nonAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Ally you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, opponentAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Ally you control");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
