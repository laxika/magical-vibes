package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplantFormTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature to its owner's hand and creates a token copy")
    void returnsCreatureAndCreatesTokenCopy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var target = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
