package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskedBlackguard;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThievesGuildEnforcer.class, MaskedBlackguard.class, GrizzlyBears.class,
        Spellbook.class, Conspiracy.class, CloakAndDagger.class})
class ThievesGuildEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry mills two cards from each opponent")
    void ownEntryMillsEachOpponent() {
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        harness.castFromHand(player1, new ThievesGuildEnforcer(), "{B}");
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
        harness.castFromHand(player1, new MaskedBlackguard(), "{1}{B}");
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
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

    @Test
    void ownEntryStillMillsWhenConspiracyReplacesItsRogueType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.setLibrary(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard()));

        harness.castFromHand(player1, new ThievesGuildEnforcer(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void noncreatureRogueEntryTriggersMill() {
        harness.addToBattlefield(player1, new ThievesGuildEnforcer());
        harness.setLibrary(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard()));

        harness.castFromHand(player1, new CloakAndDagger(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void opposingRogueDoesNotTriggerMill() {
        harness.addToBattlefield(player1, new ThievesGuildEnforcer());
        List<Card> library = List.of(new MaskedBlackguard(), new MaskedBlackguard());
        harness.setLibrary(player2, library);

        harness.castFromHand(player2, new MaskedBlackguard(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void secondEnforcerTriggersBothEnforcersAndMillsOnlyOpponent() {
        harness.addToBattlefield(player1, new ThievesGuildEnforcer());
        List<Card> ownLibrary = List.of(new MaskedBlackguard(), new MaskedBlackguard());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard(), new MaskedBlackguard()));

        harness.castFromHand(player1, new ThievesGuildEnforcer(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void flashEntryDuringOpponentsTurnEnablesThresholdWhenMillResolves() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setGraveyard(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard(), new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard()));
        harness.setLibrary(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard()));

        harness.castFromHand(player1, new ThievesGuildEnforcer(), "{B}");
        harness.passBothPriorities();
        Permanent enforcer = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void millsOnlyRemainingCardFromShortLibrary() {
        harness.setLibrary(player2, List.of(new MaskedBlackguard()));
        harness.castFromHand(player1, new ThievesGuildEnforcer(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void thresholdBonusAppliesOnlyOnceAndOnlyToEnforcer() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new ThievesGuildEnforcer());
        Permanent blackguard = harness.addToBattlefieldAndReturn(player1, new MaskedBlackguard());
        harness.setGraveyard(player2, List.of(new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard(), new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard(), new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard(), new MaskedBlackguard(), new MaskedBlackguard(),
                new MaskedBlackguard()));

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, blackguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blackguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blackguard, Keyword.DEATHTOUCH)).isFalse();
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
