package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusoryAmbusher.class, GrizzlyBears.class, Shock.class})
class IllusoryAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to noncombat damage dealt to it")
    void drawsCardsEqualToNoncombatDamage() {
        Permanent ambusher = harness.addToBattlefieldAndReturn(player2, new IllusoryAmbusher());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ambusher.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock");
        harness.assertInGraveyard(player2, "Illusory Ambusher");
    }

    @Test
    @DisplayName("Draws cards equal to combat damage dealt to it")
    void drawsCardsEqualToCombatDamage() {
        Permanent ambusher = addCreatureReady(player2, new IllusoryAmbusher());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock()));

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock");
        harness.assertInGraveyard(player2, "Illusory Ambusher");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
