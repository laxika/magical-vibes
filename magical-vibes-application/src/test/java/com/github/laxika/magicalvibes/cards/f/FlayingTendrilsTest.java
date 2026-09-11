package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlayingTendrils.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class FlayingTendrilsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 and exiles creatures that die this turn")
    void weakensCreaturesAndExilesThoseThatDie() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castTendrils();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature that dies later in the same turn")
    void replacementAppliesToLaterDeathsThisTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castTendrils();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("The -2/-2 and replacement effect expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Card card = new SerraAngel();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, card);

        castTendrils();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
    }

    private void castTendrils() {
        harness.setHand(player1, List.of(new FlayingTendrils()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
