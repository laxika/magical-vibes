package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AscendedLawmage;
import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.g.GleamOfBattle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenounceTheGuilds.class, GrizzlyBears.class, QasaliAmbusher.class,
        WoollyThoctar.class, AscendedLawmage.class, BeetleformMage.class, GleamOfBattle.class})
class RenounceTheGuildsTest extends BaseCardTest {

    @Test
    @DisplayName("Both players sacrifice their only multicolored permanent")
    void bothPlayersSacrifice() {
        harness.addToBattlefield(player1, new QasaliAmbusher());
        harness.addToBattlefield(player2, new WoollyThoctar());

        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Woolly Thoctar")));

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

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Woolly Thoctar");
    }

    @Test
    @DisplayName("A multicolored enchantment is sacrificed")
    void sacrificesNoncreaturePermanent() {
        harness.addToBattlefield(player2, new GleamOfBattle());
        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Gleam of Battle");
        harness.assertInGraveyard(player2, "Gleam of Battle");
    }

    @Test
    @DisplayName("Hexproof does not prevent a multicolored permanent from being sacrificed")
    void sacrificesHexproofPermanent() {
        harness.addToBattlefield(player2, new AscendedLawmage());
        harness.setHand(player1, List.of(new RenounceTheGuilds()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Ascended Lawmage");
        harness.assertInGraveyard(player2, "Ascended Lawmage");
    }

    @Test
    @DisplayName("Active player chooses first even when the opponent casts, and sacrifices wait for both choices")
    void bothPlayersChooseBeforeSacrificing() {
        harness.addToBattlefield(player1, new AscendedLawmage());
        harness.addToBattlefield(player1, new BeetleformMage());
        harness.addToBattlefield(player2, new AscendedLawmage());
        harness.addToBattlefield(player2, new BeetleformMage());
        harness.setHand(player2, List.of(new RenounceTheGuilds()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0);

        PendingInteraction.MultiPermanentChoice first = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(first).isNotNull();
        assertThat(first.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Ascended Lawmage")));

        harness.assertOnBattlefield(player1, "Ascended Lawmage");
        PendingInteraction.MultiPermanentChoice second = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(second).isNotNull();
        assertThat(second.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Beetleform Mage")));

        harness.assertInGraveyard(player1, "Ascended Lawmage");
        harness.assertInGraveyard(player2, "Beetleform Mage");
        harness.assertOnBattlefield(player1, "Beetleform Mage");
        harness.assertOnBattlefield(player2, "Ascended Lawmage");
    }
}
