package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RakdosKeyrune;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshZealot.class, AncientGrudge.class, RakdosKeyrune.class, AnnihilatingFire.class})
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
        harness.addToBattlefield(player1, new RakdosKeyrune());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        UUID targetId = harness.getPermanentId(player1, "Rakdos Keyrune");
        harness.castFlashback(player2, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Dealing 3 damage to its own controller when they cast from their graveyard")
    void damagesOwnControllerCastingFromGraveyard() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player2, new RakdosKeyrune());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Rakdos Keyrune");
        harness.castFlashback(player1, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Not triggering when a spell is cast from hand")
    void noDamageWhenSpellCastFromHand() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player2, new RakdosKeyrune());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Rakdos Keyrune");
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Each Ash Zealot triggers independently for a graveyard spell")
    void multipleZealotsDamageTheCaster() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player2, new AshZealot());
        harness.addToBattlefield(player1, new RakdosKeyrune());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);

        harness.castFlashback(player2, 0, harness.getPermanentId(player1, "Rakdos Keyrune"));
        resolveStack();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
        harness.assertNotOnBattlefield(player1, "Rakdos Keyrune");
    }

    @Test
    @DisplayName("The graveyard-cast trigger resolves before the triggering spell")
    void damageResolvesBeforeTheGraveyardSpell() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player1, new RakdosKeyrune());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);

        harness.castFlashback(player2, 0, harness.getPermanentId(player1, "Rakdos Keyrune"));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Rakdos Keyrune");

        resolveStack();
        harness.assertNotOnBattlefield(player1, "Rakdos Keyrune");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Removing Ash Zealot in response does not stop its damage trigger")
    void triggerStillDealsDamageAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new AshZealot());
        harness.addToBattlefield(player1, new RakdosKeyrune());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.setHand(player2, List.of(new AnnihilatingFire()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);

        harness.castFlashback(player2, 0, harness.getPermanentId(player1, "Rakdos Keyrune"));
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Ash Zealot"));
        harness.assertNotOnBattlefield(player1, "Ash Zealot");
        harness.assertLife(player2, 20);

        resolveStack();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Rakdos Keyrune");
    }
}
