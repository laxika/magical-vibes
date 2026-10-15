package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HaazdaExonerator;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellSnare.class, MistralCharger.class, HaazdaExonerator.class, Skyscribing.class})
class SpellSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a spell with mana value 2")
    void canTargetManaValue2Spell() {
        MistralCharger charger = new MistralCharger();
        harness.castFromHand(player1, charger, "{1}{W}");

        SpellSnare spellSnare = new SpellSnare();
        harness.setHand(player2, List.of(spellSnare));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, charger.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(charger.getId());
    }

    @Test
    @DisplayName("Cannot target a spell with a different mana value")
    void cannotTargetDifferentManaValueSpell() {
        HaazdaExonerator exonerator = new HaazdaExonerator();
        harness.castFromHand(player1, exonerator, "{W}");

        SpellSnare spellSnare = new SpellSnare();
        harness.setHand(player2, List.of(spellSnare));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, exonerator.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters the targeted mana value 2 spell")
    void countersManaValue2Spell() {
        MistralCharger charger = new MistralCharger();
        harness.castFromHand(player1, charger, "{1}{W}");

        SpellSnare spellSnare = new SpellSnare();
        harness.setHand(player2, List.of(spellSnare));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, charger.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player1, "Mistral Charger");
        harness.assertInGraveyard(player2, "Spell Snare");
    }

    @Test
    @DisplayName("Cannot target an X spell whose chosen mana value is greater than 2")
    void cannotTargetXSpellWithChosenManaValueGreaterThanTwo() {
        Skyscribing skyscribing = new Skyscribing();
        harness.setHand(player1, List.of(skyscribing));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 1);

        SpellSnare spellSnare = new SpellSnare();
        harness.setHand(player2, List.of(spellSnare));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, skyscribing.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters Skyscribing with X chosen as zero")
    void countersXSpellWithManaValueTwo() {
        Skyscribing skyscribing = new Skyscribing();
        harness.setHand(player1, List.of(skyscribing));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        harness.setHand(player2, List.of(new SpellSnare()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, skyscribing.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skyscribing");
        harness.assertInGraveyard(player2, "Spell Snare");
    }

    @Test
    @DisplayName("Can counter its controller's own spell")
    void countersOwnSpell() {
        MistralCharger charger = new MistralCharger();
        harness.castFromHand(player1, charger, "{1}{W}");

        harness.setHand(player1, List.of(new SpellSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, charger.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mistral Charger");
        harness.assertInGraveyard(player1, "Spell Snare");
        harness.assertNotOnBattlefield(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Does not resolve when another Spell Snare counters its target first")
    void targetLeavesStackBeforeResolution() {
        MistralCharger charger = new MistralCharger();
        harness.castFromHand(player1, charger, "{1}{W}");

        SpellSnare first = new SpellSnare();
        SpellSnare second = new SpellSnare();
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, charger.getId());
        harness.castInstant(player2, 0, charger.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        harness.assertInGraveyard(player1, "Mistral Charger");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId()).contains(second.getId()).doesNotContain(first.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertNotOnBattlefield(player1, "Mistral Charger");
    }
}
