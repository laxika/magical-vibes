package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class BarrinsUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target sharing a uniquely most common color")
    void returnsTargetWithMostCommonColor() {
        Permanent target = addColoredPermanent(player2, "White Target", CardColor.WHITE);
        addColoredPermanent(player1, "White Support", CardColor.WHITE);

        castBarrinsUnmaking(target);

        harness.assertNotOnBattlefield(player2, "White Target");
        harness.assertInHand(player2, "White Target");
    }

    @Test
    @DisplayName("Returns a target sharing a color tied for most common")
    void returnsTargetWithTiedMostCommonColor() {
        Permanent target = addColoredPermanent(player2, "White Target", CardColor.WHITE);
        addColoredPermanent(player1, "Blue Permanent", CardColor.BLUE);

        castBarrinsUnmaking(target);

        harness.assertInHand(player2, "White Target");
    }

    @Test
    @DisplayName("Does nothing when the target shares no most common color")
    void doesNothingForLessCommonColor() {
        Permanent target = addColoredPermanent(player2, "White Target", CardColor.WHITE);
        addColoredPermanent(player1, "Blue Permanent 1", CardColor.BLUE);
        addColoredPermanent(player1, "Blue Permanent 2", CardColor.BLUE);

        castBarrinsUnmaking(target);

        harness.assertOnBattlefield(player2, "White Target");
        harness.assertNotInHand(player2, "White Target");
    }

    @Test
    @DisplayName("Checks the most common color when the spell resolves")
    void checksMostCommonColorAtResolution() {
        Permanent target = addColoredPermanent(player2, "White Target", CardColor.WHITE);
        harness.setHand(player1, List.of(new BarrinsUnmaking()));
        addCastingMana();
        harness.castInstant(player1, 0, target.getId());

        addColoredPermanent(player1, "Blue Permanent 1", CardColor.BLUE);
        addColoredPermanent(player1, "Blue Permanent 2", CardColor.BLUE);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "White Target");
        harness.assertNotInHand(player2, "White Target");
    }

    private Permanent addColoredPermanent(Player owner, String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(colors.length == 0 ? null : colors[0]);
        card.setColors(List.of(colors));
        card.setPower(1);
        card.setToughness(1);
        return harness.addToBattlefieldAndReturn(owner, card);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castBarrinsUnmaking(Permanent target) {
        harness.setHand(player1, List.of(new BarrinsUnmaking()));
        addCastingMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
