package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VilisBrokerOfBlood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CertainDeath.class, CrossroadsConsecrator.class, VilisBrokerOfBlood.class})
class CertainDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature, its controller loses 2 life, and you gain 2 life")
    void destroysCreatureAndExchangesLife() {
        harness.addToBattlefield(player2, new CrossroadsConsecrator());
        harness.setHand(player1, List.of(new CertainDeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Crossroads Consecrator");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Crossroads Consecrator");
        harness.assertInGraveyard(player2, "Crossroads Consecrator");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CrossroadsConsecrator());
        harness.setHand(player1, List.of(new CertainDeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Crossroads Consecrator");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can destroy your own creature and both life changes affect you")
    void destroysOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new CrossroadsConsecrator()).getId();
        harness.setHand(player1, List.of(new CertainDeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Crossroads Consecrator");
        harness.assertNotOnBattlefield(player1, "Crossroads Consecrator");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Vilis is destroyed before its controller loses life and cannot trigger")
    void destroysVilisBeforeLifeLoss() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VilisBrokerOfBlood()).getId();
        harness.setHand(player1, List.of(new CertainDeath()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CrossroadsConsecrator(), new CrossroadsConsecrator()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Vilis, Broker of Blood");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
