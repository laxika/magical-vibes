package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AvacynAngelOfHope;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrantOfDiscord.class, Forest.class, NettleSwine.class,
        SigardaHostOfHerons.class, AvacynAngelOfHope.class})
class TyrantOfDiscordTest extends BaseCardTest {

    private void castTyrant() {
        harness.setHand(player1, List.of(new TyrantOfDiscord()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB stops after a land is sacrificed at random")
    void etbStopsOnLand() {
        harness.addToBattlefield(player2, new Forest());

        castTyrant();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB repeats while nonland permanents are sacrificed")
    void etbRepeatsOnNonlandPermanents() {
        harness.addToBattlefield(player2, new NettleSwine());
        harness.addToBattlefield(player2, new NettleSwine());

        castTyrant();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB does nothing when the target opponent controls no permanents")
    void etbNoPermanents() {
        castTyrant();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only one land is sacrificed when the opponent controls multiple lands")
    void stopsAfterFirstLandAndLeavesControllersPermanentsAlone() {
        harness.addToBattlefield(player1, new NettleSwine());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castTyrant();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Nettle Swine");
        harness.assertOnBattlefield(player1, "Tyrant of Discord");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sigarda prevents every sacrifice and the process ends")
    void sigardaPreventsSacrifices() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new NettleSwine());

        castTyrant();

        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Nettle Swine");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible does not prevent sacrifice or repeating the process")
    void sacrificesIndestructibleNonlands() {
        harness.addToBattlefield(player2, new AvacynAngelOfHope());
        harness.addToBattlefield(player2, new NettleSwine());

        castTyrant();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Avacyn, Angel of Hope");
        harness.assertInGraveyard(player2, "Nettle Swine");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @DisplayName("The random sacrifices go on after each Gravebane Zombie's library-or-Kalitas choice")
    void processRepeatsAfterEachDeathReplacementChoice() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setLibrary(player2, List.of());

        castTyrant();

        var firstChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(first, second);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getCard().getId(), second.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
