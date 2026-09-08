package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BafflingEndTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent creature with mana value 3 or less")
    void etbExilesSmallOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castBafflingEnd(bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an opponent creature with mana value greater than 3")
    void cannotTargetLargeOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, giantId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Baffling End leaves, a target opponent creates a 3/3 Dinosaur with trample")
    void leavingBattlefieldGivesTargetOpponentDinosaur() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castBafflingEnd(bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID bafflingEndId = harness.getPermanentId(player1, "Baffling End");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bafflingEndId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent dinosaur = findPermanent(player2, "Dinosaur");
        assertThat(dinosaur.getEffectivePower()).isEqualTo(3);
        assertThat(dinosaur.getEffectiveToughness()).isEqualTo(3);
        assertThat(dinosaur.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(dinosaur.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    private void castBafflingEnd(UUID targetId) {
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, targetId);
    }
}
