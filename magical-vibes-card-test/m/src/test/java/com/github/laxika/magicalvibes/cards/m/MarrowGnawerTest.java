package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JukaiMessenger;
import com.github.laxika.magicalvibes.cards.n.NezumiRonin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarrowGnawer.class, NezumiRonin.class, JukaiMessenger.class})
class MarrowGnawerTest extends BaseCardTest {

    @Test
    @DisplayName("All Rats have fear, including opponents' Rats and itself")
    void allRatsHaveFear() {
        Permanent gnawer = addReadyGnawer(player1);
        harness.addToBattlefield(player1, new NezumiRonin());
        harness.addToBattlefield(player2, new NezumiRonin());
        harness.addToBattlefield(player2, new JukaiMessenger());

        assertThat(gqs.hasKeyword(gd, gnawer, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, ratOf(player1), Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, ratOf(player2), Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Jukai Messenger"), Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Rats lose fear once Marrow-Gnawer leaves the battlefield")
    void fearEndsWhenGnawerLeaves() {
        Permanent gnawer = addReadyGnawer(player1);
        harness.addToBattlefield(player2, new NezumiRonin());

        assertThat(gqs.hasKeyword(gd, ratOf(player2), Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(gnawer);

        assertThat(gqs.hasKeyword(gd, ratOf(player2), Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Rat creates one token per Rat still controlled")
    void abilityCreatesTokenPerRat() {
        addReadyGnawer(player1);
        harness.addToBattlefield(player1, new NezumiRonin());
        harness.addToBattlefield(player1, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ratOf(player1).getId());
        harness.passBothPriorities();

        // Gnawer + surviving Nezumi Ronin = 2 Rats at resolution -> 2 tokens, 4 Rats total.
        assertThat(countRats(player1)).isEqualTo(4);
        assertThat(countPermanents(player1, "Rat")).isEqualTo(2);
    }

    @Test
    @DisplayName("Created Rat tokens also have fear")
    void createdTokensHaveFear() {
        addReadyGnawer(player1);
        harness.addToBattlefield(player1, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ratOf(player1).getId());
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Rat");
        assertThat(tokens).isNotEmpty();
        assertThat(tokens).allMatch(token -> gqs.hasKeyword(gd, token, Keyword.FEAR));
    }

    @Test
    @DisplayName("X counts only Rats controlled by the activating player")
    void abilityCountsOnlyControlledRats() {
        addReadyGnawer(player1);
        Permanent ownRat = harness.addToBattlefieldAndReturn(player1, new NezumiRonin());
        harness.addToBattlefield(player2, new NezumiRonin());
        harness.addToBattlefield(player2, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ownRat.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rat")).isEqualTo(1);
        assertThat(countRats(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only a Rat can be sacrificed as the activation cost")
    void cannotSacrificeNonRat() {
        addReadyGnawer(player1);
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new NezumiRonin());
        Permanent nonRat = harness.addToBattlefieldAndReturn(player1, new JukaiMessenger());

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonRat.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, rat.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Marrow-Gnawer can sacrifice itself, leaving no Rats and creating no tokens")
    void sacrificingItselfCreatesNoTokens() {
        Permanent gnawer = addReadyGnawer(player1);

        harness.activateAbility(player1, 0, null, null);
        UUID gnawerId = gnawer.getId();
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, gnawerId);
        }
        harness.passBothPriorities();

        assertThat(countRats(player1)).isZero();
    }

    @Test
    @DisplayName("Rats entering before resolution increase the number of tokens")
    void countsRatsAtResolution() {
        addReadyGnawer(player1);
        Permanent sacrificedRat = harness.addToBattlefieldAndReturn(player1, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedRat.getId());
        harness.addToBattlefield(player1, new NezumiRonin());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rat")).isEqualTo(2);
        assertThat(countRats(player1)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing Marrow-Gnawer still creates tokens for the surviving Rats")
    void sacrificingSourceStillResolves() {
        Permanent gnawer = addReadyGnawer(player1);
        harness.addToBattlefield(player1, new NezumiRonin());
        harness.addToBattlefield(player1, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gnawer.getId());
        harness.assertInGraveyard(player1, "Marrow-Gnawer");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rat")).isEqualTo(2);
        assertThat(countRats(player1)).isEqualTo(4);
        assertThat(findPermanents(player1, "Rat"))
                .allMatch(token -> !gqs.hasKeyword(gd, token, Keyword.FEAR));
    }

    @Test
    @DisplayName("An opponent's Rat cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsRat() {
        addReadyGnawer(player1);
        Permanent ownRat = harness.addToBattlefieldAndReturn(player1, new NezumiRonin());
        Permanent opponentsRat = harness.addToBattlefieldAndReturn(player2, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsRat.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownRat.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rat")).isEqualTo(1);
        assertThat(countRats(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new MarrowGnawer());
        harness.addToBattlefield(player1, new NezumiRonin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countRats(player1)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability taps Marrow-Gnawer")
    void activationPaysTapCost() {
        Permanent gnawer = addReadyGnawer(player1);
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new NezumiRonin());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, rat.getId());

        assertThat(gnawer.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Rat")).isEqualTo(1);
    }
    private Permanent addReadyGnawer(Player player) {
        return addCreatureReady(player, new MarrowGnawer());
    }

    private Permanent ratOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.RAT))
                .filter(p -> !"Marrow-Gnawer".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private long countRats(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.RAT))
                .count();
    }

}
