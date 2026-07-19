package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

class AncientZigguratTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    @Test
    @DisplayName("Tapping prompts for a mana color choice without using the stack")
    void tappingPromptsColorChoice() {
        harness.addToBattlefield(player1, new AncientZiggurat());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent ziggurat = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ziggurat.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds creature-spell-only mana, not regular mana")
    void choosingColorAddsCreatureSpellMana() {
        harness.addToBattlefield(player1, new AncientZiggurat());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell of any type")
    void manaCanCastCreatureSpell() {
        harness.addToBattlefield(player1, new AncientZiggurat());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.WHITE, 1);

        Card bird = createCreature("Test Bird", "{W}", CardColor.WHITE, CardSubtype.BIRD);
        harness.setHand(player1, List.of(bird));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Bird");
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a subtypeless creature spell")
    void manaCanCastSubtypelessCreature() {
        harness.addToBattlefield(player1, new AncientZiggurat());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.GREEN, 1);

        Card creature = createCreature("Test Beast", "{G}", CardColor.GREEN);
        harness.setHand(player1, List.of(creature));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        harness.addToBattlefield(player1, new AncientZiggurat());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOnlyMana(ManaColor.RED, 1);

        Card bolt = new Card();
        bolt.setName("Test Bolt");
        bolt.setType(CardType.INSTANT);
        bolt.setManaCost("{R}");
        bolt.setColor(CardColor.RED);
        harness.setHand(player1, List.of(bolt));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature-spell-only mana supplements regular mana for a creature spell")
    void creatureSpellManaSupplementsRegularMana() {
        harness.addToBattlefield(player1, new AncientZiggurat());
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.addCreatureSpellOnlyMana(ManaColor.BLUE, 1);

        Card creature = createCreature("Test Elf", "{1}{G}", CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(creature));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        // The generic {1} is paid from the creature-spell-only bucket, the {G} from regular mana.
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(0);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(0);
    }
}
