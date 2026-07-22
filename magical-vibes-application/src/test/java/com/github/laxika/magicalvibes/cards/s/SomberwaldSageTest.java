package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SomberwaldSageTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Tapping prompts for a mana color choice without using the stack")
    void tappingPromptsColorChoice() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds three creature-spell-only mana")
    void choosingColorAddsThreeCreatureSpellMana() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SomberwaldSage());
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell")
    void manaCanCastCreatureSpell() {
        harness.addToBattlefield(player1, new SomberwaldSage());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.GREEN, 3);

        Card beast = createCreature("Test Beast", "{2}{G}", CardColor.GREEN);
        harness.setHand(player1, List.of(beast));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Beast");
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        harness.addToBattlefield(player1, new SomberwaldSage());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.GREEN, 3);

        Card ramp = new Card();
        ramp.setName("Test Ramp");
        ramp.setType(CardType.SORCERY);
        ramp.setManaCost("{G}");
        ramp.setColor(CardColor.GREEN);
        harness.setHand(player1, List.of(ramp));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
