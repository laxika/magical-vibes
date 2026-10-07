package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thragtusk.class, Unsummon.class})
class ThragtuskTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 5 life")
    void entryGainsFiveLife() {
        harness.castFromHand(player1, new Thragtusk(), "{4}{G}");
        harness.passBothPriorities(); // creature resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Leaving the battlefield creates a 3/3 green Beast token")
    void leavingCreatesBeastToken() {
        harness.addToBattlefield(player1, new Thragtusk());

        Permanent thragtusk = findPermanent(player1, "Thragtusk");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, thragtusk));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.BEAST))
                .toList();

        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(beasts.getFirst().getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Returning Thragtusk to hand creates a Beast for its controller")
    void returningToHandCreatesTokenForController() {
        harness.addToBattlefield(player2, new Thragtusk());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Thragtusk"));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Thragtusk");
        harness.assertNotOnBattlefield(player2, "Thragtusk");
        assertThat(countPermanents(player2, "Beast")).isZero();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Beast")).hasSize(1);
        Permanent beast = findPermanent(player2, "Beast");
        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(countPermanents(player1, "Beast")).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entry trigger still gains life after Thragtusk leaves")
    void entryTriggerResolvesAfterSourceLeaves() {
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castFromHand(player1, new Thragtusk(), "{4}{G}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Thragtusk"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Thragtusk");
        resolveAllTriggers();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(countPermanents(player2, "Beast")).isZero();
    }
}
