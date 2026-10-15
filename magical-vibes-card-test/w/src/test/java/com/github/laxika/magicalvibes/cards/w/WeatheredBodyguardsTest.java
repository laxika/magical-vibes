package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PsionicSliver;
import com.github.laxika.magicalvibes.cards.p.PentarchWard;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeatheredBodyguards.class, PsionicSliver.class, PentarchWard.class})
class WeatheredBodyguardsTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Bodyguards redirect combat damage from unblocked creatures")
    void untappedBodyguardsRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguards.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped Bodyguards do not redirect combat damage")
    void tappedBodyguardsDoNotRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        bodyguards.tap();
        addUnblockedAttacker(player1);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blocked creature combat damage is not redirected")
    void blockedDamageIsNotRedirected() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Bodyguards that lose all abilities do not redirect combat damage")
    void bodyguardsWithoutAbilitiesDoNotRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        bodyguards.setLosesAllAbilitiesUntilEndOfTurn(true);
        addUnblockedAttacker(player1);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Trample damage from a blocked creature is not redirected")
    void trampleDamageFromBlockedCreatureIsNotRedirected() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());

        WeatheredBodyguards attackerCard = new WeatheredBodyguards();
        attackerCard.setPower(6);
        attackerCard.setKeywords(Set.of(Keyword.TRAMPLE));
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WeatheredBodyguards());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Weathered Bodyguards do not redirect noncombat damage")
    void noncombatDamageIsNotRedirected() {
        Permanent bodyguards = addCreatureReady(player2, new WeatheredBodyguards());
        addCreatureReady(player1, new PsionicSliver());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new WeatheredBodyguards()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bodyguards = findPermanent(player1, "Weathered Bodyguards");
        assertThat(bodyguards.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bodyguards));
        harness.passBothPriorities();

        assertThat(bodyguards.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Face-down Bodyguards do not redirect combat damage")
    void faceDownBodyguardsDoNotRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        bodyguards.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        addUnblockedAttacker(player1);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Turning Bodyguards face up before damage enables redirection immediately")
    void turningFaceUpEnablesRedirection() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        bodyguards.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        addUnblockedAttacker(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.turnFaceUp(player2, 0);
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(bodyguards.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("All simultaneous unblocked damage is redirected even when it is lethal")
    void redirectsAllSimultaneousDamage() {
        harness.addToBattlefield(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Weathered Bodyguards");
        harness.assertInGraveyard(player2, "Weathered Bodyguards");
    }

    @Test
    @DisplayName("Defending player chooses which Bodyguards redirect damage")
    void multipleBodyguardsRequireReplacementChoice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection prevents redirected damage from a matching source")
    void protectionPreventsRedirectedDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        Permanent ward = harness.addToBattlefieldAndReturn(player2, new PentarchWard());
        ward.setAttachedTo(bodyguards.getId());
        ward.setChosenColor(CardColor.WHITE);
        addUnblockedAttacker(player1);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    private Permanent addUnblockedAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new WeatheredBodyguards());
        attacker.setAttacking(true);
        return attacker;
    }
}
