package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AzoriusSkyguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.g.GleamingGeardrake;
import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.cards.m.MakeYourMove;
import com.github.laxika.magicalvibes.cards.r.RakdosCackler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivMizzetGuildpact.class, AzoriusSkyguard.class, RakdosCackler.class, Forest.class,
        DogWalker.class, GleamingGeardrake.class, LightningHelix.class, MakeYourMove.class})
class NivMizzetGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage uses each distinct exactly-two-color pair for damage, draw, and life gain")
    void combatDamageUsesDistinctColorPairs() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player1, new RakdosCackler());
        harness.addToBattlefield(player1, new RakdosCackler());
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        niv.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentCanTargetWithMonocoloredSpell() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.setHand(player2, List.of(new MakeYourMove()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, niv.getId());

        harness.assertNotOnBattlefield(player1, "Niv-Mizzet, Guildpact");
        harness.assertInGraveyard(player1, "Niv-Mizzet, Guildpact");
    }

    @Test
    void opponentCannotTargetWithMulticoloredSpell() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.setHand(player2, List.of(new LightningHelix()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, niv.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(niv.getMarkedDamage()).isZero();
    }

    @Test
    void controllerCanTargetWithMulticoloredSpell() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, niv.getId());

        assertThat(niv.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 23);
    }

    @Test
    void zeroPairsStillTriggersButDoesNotDamageDrawOrGainLife() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player2, new DogWalker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        niv.setAttacking(true);
        resolveCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void samePlayerCanBeDamageAndDrawTarget() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player1, new DogWalker());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        niv.setAttacking(true);
        resolveCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void lethalDamageToOwnColorPairPermanentDoesNotReduceDrawOrLifeGain() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player1, new DogWalker());
        Permanent walker = findPermanent(player1, "Dog Walker");
        harness.addToBattlefield(player1, new GleamingGeardrake());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        niv.setAttacking(true);
        resolveCombat();

        harness.handlePermanentChosen(player1, walker.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dog Walker");
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void colorPairsAreCountedAtResolutionAfterResponseRemovesPermanent() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player1, new DogWalker());
        Permanent walker = findPermanent(player1, "Dog Walker");
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningHelix()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        niv.setAttacking(true);
        resolveCombat();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        harness.castInstant(player2, 0, walker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dog Walker");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
