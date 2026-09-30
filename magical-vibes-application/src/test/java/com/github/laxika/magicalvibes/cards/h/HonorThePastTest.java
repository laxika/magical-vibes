package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorThePast.class, ColossalDreadmaw.class, HolyDay.class, ScornfulEgotist.class})
class HonorThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 6 or less as a noncreature artifact")
    void returnsCreatureAsNoncreatureArtifact() {
        Card creature = new ColossalDreadmaw();
        harness.setGraveyard(player1, List.of(creature));
        castHonorThePast(creature);

        Permanent returned = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        harness.assertNotInGraveyard(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 6")
    void cannotTargetCreatureWithManaValueGreaterThanSix() {
        Card creature = new ScornfulEgotist();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HonorThePast()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Card card = new HolyDay();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new HonorThePast()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHonorThePast(Card target) {
        harness.setHand(player1, List.of(new HonorThePast()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
