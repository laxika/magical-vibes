package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenounceTheGuildsTest extends BaseCardTest {

    @Test
    @DisplayName("Both players sacrifice their only multicolored permanent")
    void bothPlayersSacrifice() {
        harness.addToBattlefield(player1, new QasaliAmbusher());
        harness.addToBattlefield(player2, new WoollyThoctar());

        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
        harness.assertNotOnBattlefield(player2, "Woolly Thoctar");
    }

    @Test
    @DisplayName("Monocolored permanents are never sacrificed")
    void monocoloredUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new WoollyThoctar());

        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Woolly Thoctar");
    }

    @Test
    @DisplayName("A player with several multicolored permanents chooses which one to sacrifice")
    void playerWithSeveralChooses() {
        harness.addToBattlefield(player2, new WoollyThoctar());
        harness.addToBattlefield(player2, new QasaliAmbusher());

        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent thoctar = findPermanent(player2, "Woolly Thoctar");
        harness.handleMultiplePermanentsChosen(player2, List.of(thoctar.getId()));

        harness.assertNotOnBattlefield(player2, "Woolly Thoctar");
        harness.assertOnBattlefield(player2, "Qasali Ambusher");
    }

    @Test
    @DisplayName("A player controlling no multicolored permanent sacrifices nothing")
    void playerWithNoneUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WoollyThoctar());

        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Woolly Thoctar");
    }
}
