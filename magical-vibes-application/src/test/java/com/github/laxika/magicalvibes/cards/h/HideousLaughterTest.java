package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HideousLaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2, killing X/2s on both sides")
    void shrinksAllCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new HideousLaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new HideousLaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        addCreatureReady(player2, new GrizzlyBears());
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        HideousLaughter hideousLaughter = new HideousLaughter();
        harness.setHand(player1, List.of(arcaneShock, hideousLaughter));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hideousLaughter);
    }
}
