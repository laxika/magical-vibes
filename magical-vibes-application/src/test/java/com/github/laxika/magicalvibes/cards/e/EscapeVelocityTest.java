package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({EscapeVelocity.class, GrizzlyBears.class})
class EscapeVelocityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and haste")
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new EscapeVelocity());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Escape exiles two other graveyard cards and enchants a creature")
    void escapeExilesTwoOtherCardsAndEnchantsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card escapeVelocity = new EscapeVelocity();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(escapeVelocity, first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, bears.getId(), List.of(), List.of(1, 2));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);

        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Escape Velocity");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Escape requires two other cards in the graveyard")
    void escapeRequiresTwoOtherCards() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EscapeVelocity(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, bears.getId(), List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
