package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({FanaticalOffering.class, GrizzlyBears.class, Island.class, Spellbook.class})
class FanaticalOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature draws two cards and creates a Map token")
    void sacrificesCreatureDrawsTwoAndCreatesMap() {
        Card first = new Spellbook();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent map = findPermanents(player1, "Map").getFirst();
        assertThat(map.getCard().isToken()).isTrue();
        assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP);
    }

    @Test
    @DisplayName("Sacrificing an artifact is also a legal additional cost")
    void sacrificesArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice a permanent that is neither an artifact nor a creature")
    void rejectsInvalidSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }
}
