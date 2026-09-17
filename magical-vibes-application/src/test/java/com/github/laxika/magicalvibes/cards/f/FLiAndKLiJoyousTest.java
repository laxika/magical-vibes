package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FLiAndKLiJoyous.class)
class FLiAndKLiJoyousTest extends BaseCardTest {

    private static final Set<CardSubtype> ALLOWED_SUBTYPES =
            Set.of(CardSubtype.DWARF, CardSubtype.EQUIPMENT, CardSubtype.SAGA);

    @Test
    @DisplayName("The mana ability adds two red mana for Dwarf, Equipment, and Saga spells")
    void addsRestrictedRedMana() {
        addReadySource();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaForColor(ALLOWED_SUBTYPES, ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The restricted mana casts a Dwarf spell")
    void restrictedManaCastsDwarfSpell() {
        addRestrictedMana();
        harness.setHand(player1, List.of(createSpell("Dwarf spell", CardType.CREATURE, CardSubtype.DWARF)));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted mana casts an Equipment spell")
    void restrictedManaCastsEquipmentSpell() {
        addRestrictedMana();
        harness.setHand(player1, List.of(createSpell("Equipment spell", CardType.ARTIFACT, CardSubtype.EQUIPMENT)));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted mana casts a Saga spell")
    void restrictedManaCastsSagaSpell() {
        addRestrictedMana();
        harness.setHand(player1, List.of(createSpell("Saga spell", CardType.ENCHANTMENT, CardSubtype.SAGA)));

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted mana cannot cast another spell")
    void restrictedManaCannotCastOtherSpell() {
        addReadySource();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(createSpell("Elf spell", CardType.CREATURE, CardSubtype.ELF)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addReadySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FLiAndKLiJoyous());
        source.setSummoningSick(false);
        return source;
    }

    private void addRestrictedMana() {
        addReadySource();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
    }

    private static Card createSpell(String name, CardType type, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setSubtypes(List.of(subtype));
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        return card;
    }
}
