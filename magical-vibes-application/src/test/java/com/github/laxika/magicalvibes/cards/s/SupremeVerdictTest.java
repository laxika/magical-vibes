package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SupremeVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures but not other permanents")
    void destroysAllCreaturesButNotOtherPermanents() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new SupremeVerdict()));
        addVerdictMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SupremeVerdict verdict = new SupremeVerdict();

        harness.setHand(player1, List.of(verdict));
        addVerdictMana(player1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, verdict.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Supreme Verdict");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addVerdictMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
