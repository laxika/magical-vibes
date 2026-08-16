package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskedBlackguard;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThievesGuildEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry mills two cards from each opponent")
    void ownEntryMillsEachOpponent() {
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        harness.setHand(player1, List.of(new ThievesGuildEnforcer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Another Rogue you control also triggers the mill")
    void anotherRogueTriggersMill() {
        harness.addToBattlefield(player1, new ThievesGuildEnforcer());
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        harness.setHand(player1, List.of(new MaskedBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-Rogue entering does not trigger the mill")
    void nonRogueDoesNotTriggerMill() {
        harness.addToBattlefield(player1, new ThievesGuildEnforcer());
        List<Card> library = List.of(new Spellbook(), new Spellbook());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gets +2/+1 and deathtouch while an opponent has eight cards in their graveyard")
    void thresholdBoostAndDeathtouch() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new ThievesGuildEnforcer());
        harness.setGraveyard(player2, graveyardOfSize(7));

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isFalse();

        harness.setGraveyard(player2, graveyardOfSize(8));

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isTrue();

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Its controller's graveyard does not enable the threshold ability")
    void ownGraveyardDoesNotEnableThreshold() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new ThievesGuildEnforcer());
        harness.setGraveyard(player1, graveyardOfSize(8));

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isFalse();
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
