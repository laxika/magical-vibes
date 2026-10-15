package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DreamSalvage;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GutturalResponse.class, Opt.class, Shock.class, Divination.class, DreamSalvage.class})
class GutturalResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a blue instant spell")
    void countersBlueInstant() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new GutturalResponse()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, opt.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a red instant spell")
    void cannotTargetRedInstant() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new GutturalResponse()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a blue sorcery spell")
    void cannotTargetBlueSorcery() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new GutturalResponse()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, divination.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green mana counters a blue hybrid instant paid for with black mana")
    void countersBlueHybridInstantWithGreenMana() {
        DreamSalvage salvage = new DreamSalvage();
        harness.setHand(player1, List.of(salvage));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new GutturalResponse()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, salvage.getId());

        harness.assertInGraveyard(player1, "Dream Salvage");
        harness.assertInGraveyard(player2, "Guttural Response");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter a blue instant controlled by its own caster")
    void countersOwnBlueInstant() {
        DreamSalvage salvage = new DreamSalvage();
        harness.setHand(player1, List.of(salvage, new GutturalResponse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, salvage.getId());

        harness.assertInGraveyard(player1, "Dream Salvage");
        harness.assertInGraveyard(player1, "Guttural Response");
        assertThat(gd.stack).isEmpty();
    }
}
