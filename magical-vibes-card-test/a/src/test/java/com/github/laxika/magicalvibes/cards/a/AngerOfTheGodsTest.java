package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GodsWilling;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.p.PalisadeGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SilentArtisan;
import com.github.laxika.magicalvibes.cards.s.SipOfHemlock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngerOfTheGods.class, GrizzlyBears.class, SerraAngel.class,
        SilentArtisan.class, SipOfHemlock.class, GodsWilling.class,
        HealingSalve.class, PalisadeGiant.class})
class AngerOfTheGodsTest extends BaseCardTest {

    private void castAnger() {
        harness.setHand(player1, List.of(new AngerOfTheGods()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Creatures killed by the damage are exiled instead of going to the graveyard")
    void killedCreaturesAreExiled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAnger();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creatures that survive the damage remain on the battlefield")
    void survivingCreaturesRemain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAnger();

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(survivor.getMarkedDamage()).isEqualTo(3);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Players are not dealt damage")
    void playersAreNotDamaged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAnger();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A damaged survivor destroyed later in the same turn is exiled")
    void survivorDestroyedLaterThisTurnIsExiled() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new SilentArtisan());

        castAnger();
        harness.assertOnBattlefield(player2, "Silent Artisan");
        assertThat(survivor.getMarkedDamage()).isEqualTo(3);

        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, survivor.getId());

        harness.assertNotOnBattlefield(player2, "Silent Artisan");
        harness.assertNotInGraveyard(player2, "Silent Artisan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(survivor.getCard().getId()));
    }

    @Test
    @DisplayName("The exile replacement expires at the end of the turn")
    void survivorDestroyedNextTurnGoesToGraveyard() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new SilentArtisan());

        castAnger();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new SipOfHemlock()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player2, 0, survivor.getId());

        harness.assertNotOnBattlefield(player2, "Silent Artisan");
        harness.assertInGraveyard(player2, "Silent Artisan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(survivor.getCard().getId()));
    }

    @Test
    @DisplayName("Creatures entering after resolution are not subject to the exile replacement")
    void creatureEnteringAfterResolutionGoesToGraveyard() {
        castAnger();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player2, new SilentArtisan());

        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, newcomer.getId());

        harness.assertNotOnBattlefield(player2, "Silent Artisan");
        harness.assertInGraveyard(player2, "Silent Artisan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(newcomer.getCard().getId()));
    }

    @Test
    @DisplayName("Protection prevents the damage and leaves no exile replacement")
    void creatureProtectedFromRedGoesToGraveyardWhenDestroyed() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new SilentArtisan());
        harness.setHand(player2, List.of(new GodsWilling()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());
        harness.handleListChoice(player2, "RED");
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        castAnger();

        harness.assertOnBattlefield(player2, "Silent Artisan");
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, protectedCreature.getId());

        harness.assertNotOnBattlefield(player2, "Silent Artisan");
        harness.assertInGraveyard(player2, "Silent Artisan");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(protectedCreature.getCard().getId()));
    }

    @Test
    @DisplayName("A creature dealt only redirected damage is exiled if destroyed later that turn")
    void redirectedDamageRecipientIsExiledWhenDestroyed() {
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN, () -> {
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new PalisadeGiant());
            harness.addToBattlefield(player1, new SilentArtisan());
            harness.addToBattlefield(player1, new SilentArtisan());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castInstant(player1, 0, 1, giant.getId());
            harness.passBothPriorities();

            castAnger();

            harness.assertOnBattlefield(player1, "Palisade Giant");
            assertThat(giant.getMarkedDamage()).isEqualTo(6);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Silent Artisan"))
                    .allSatisfy(p -> assertThat(p.getMarkedDamage()).isZero());

            harness.setHand(player1, List.of(new SipOfHemlock()));
            harness.addMana(player1, ManaColor.BLACK, 6);
            harness.castAndResolveSorcery(player1, 0, giant.getId());

            harness.assertNotOnBattlefield(player1, "Palisade Giant");
            harness.assertNotInGraveyard(player1, "Palisade Giant");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getId().equals(giant.getCard().getId()));
        });
    }
}
