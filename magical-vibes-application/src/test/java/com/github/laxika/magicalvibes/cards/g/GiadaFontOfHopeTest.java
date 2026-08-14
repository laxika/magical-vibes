package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiadaFontOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Another Angel enters with a counter for each Angel already controlled")
    void angelEntersWithCountersForAngelsAlreadyControlled() {
        addCreatureReady(player1, new GiadaFontOfHope());
        harness.addToBattlefield(player1, createCreature("Existing Angel", "{1}", CardSubtype.ANGEL));

        Card enteringAngel = createCreature("Entering Angel", "{1}", CardSubtype.ANGEL);
        harness.setHand(player1, List.of(enteringAngel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Entering Angel");
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Angel creatures do not get Giada's entry counters")
    void nonAngelDoesNotGetEntryCounter() {
        addCreatureReady(player1, new GiadaFontOfHope());

        Card enteringCreature = createCreature("Entering Human", "{1}", CardSubtype.HUMAN);
        harness.setHand(player1, List.of(enteringCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Entering Human");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Giada's mana can cast Angels but not non-Angel spells")
    void manaIsRestrictedToAngelSpells() {
        Permanent giada = addCreatureReady(player1, new GiadaFontOfHope());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.ANGEL), ManaColor.WHITE))
                .isEqualTo(1);
        assertThat(giada.isTapped()).isTrue();

        harness.setHand(player1, List.of(createCreature("Non-Angel Spell", "{W}", CardSubtype.HUMAN)));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(createCreature("Angel Spell", "{W}", CardSubtype.ANGEL)));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Angel Spell")).isNotNull();
    }

    private static Card createCreature(String name, String manaCost, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
