package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.t.ThreefoldThunderhulk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbuelosAwakening.class, BanishingLight.class, CharcoalDiamond.class, HolyDay.class, Pacifism.class,
        SoulWarden.class, ThreefoldThunderhulk.class})
class AbuelosAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an artifact as a 1/1 Spirit with flying and X counters")
    void returnsArtifactWithAnimationAndCounters() {
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new AbuelosAwakening()));
        addMana(2);

        harness.castSorcery(player1, 0, 2, artifact.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, artifact.getName());
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a non-Aura enchantment")
    void returnsNonAuraEnchantment() {
        Card enchantment = new BanishingLight();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new AbuelosAwakening()));
        addMana(0);

        harness.castSorcery(player1, 0, 0, enchantment.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, enchantment.getName());
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an Aura or a nonpermanent card")
    void rejectsAuraAndNonpermanent() {
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new AbuelosAwakening()));
        addMana(0);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);

        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3 + xValue);
    }

    @Test
    @DisplayName("The returned noncreature artifact enters as a creature")
    void returnedArtifactTriggersCreatureEntryAbilities() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new AbuelosAwakening()));
        addMana(0);

        harness.castSorcery(player1, 0, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(findPermanent(player1, artifact.getName()).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds X counters to intrinsic entry counters and sets base power to one")
    void retainsIntrinsicEntryCountersAndCreatureSubtype() {
        Card artifact = new ThreefoldThunderhulk();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new AbuelosAwakening()));
        addMana(2);

        harness.castSorcery(player1, 0, 2, artifact.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, artifact.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.GNOME, CardSubtype.SPIRIT);
        assertThat(countPermanents(player1, "Gnome")).isEqualTo(6);
    }
}
