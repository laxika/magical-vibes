package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionDiviner.class, FugitiveWizard.class, GrizzlyBears.class, Shock.class})
class ExpeditionDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it dies while you control another Wizard")
    void drawsWhenAnotherWizardIsControlled() {
        Permanent diviner = addCreatureReady(player1, new ExpeditionDiviner());
        addCreatureReady(player1, new FugitiveWizard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithShock(player2, diviner);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw when you control no other Wizard")
    void doesNotDrawWithoutAnotherWizard() {
        Permanent diviner = addCreatureReady(player1, new ExpeditionDiviner());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithShock(player2, diviner);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
