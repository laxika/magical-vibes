package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AshZealotTest extends BaseCardTest {

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Dealing 3 damage to an opponent who casts a spell from their graveyard")
    void damagesOpponentCastingFromGraveyard() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castFlashback(player2, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Dealing 3 damage to its own controller when they cast from their graveyard")
    void damagesOwnControllerCastingFromGraveyard() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Not triggering when a spell is cast from hand")
    void noDamageWhenSpellCastFromHand() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
