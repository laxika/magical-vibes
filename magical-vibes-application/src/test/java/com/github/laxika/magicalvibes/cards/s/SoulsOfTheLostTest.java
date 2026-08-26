package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulsOfTheLost.class, GrizzlyBears.class, Shock.class})
class SoulsOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Can discard a card as its additional cost")
    void canDiscardAsAdditionalCost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SoulsOfTheLost(), new Shock()));
        addSoulsMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(souls).isNotNull();
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Can sacrifice a permanent as its additional cost")
    void canSacrificeAsAdditionalCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        harness.castSorceryWithSacrifice(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(souls).isNotNull();
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    private void addSoulsMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findSouls(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Souls of the Lost"))
                .findFirst()
                .orElse(null);
    }
}
