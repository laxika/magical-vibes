package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({ReefPirates.class, HermeticStudy.class, Unsummon.class})
class ReefPiratesTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to an opponent makes that player mill a card")
    void combatDamageTriggersMill() {
        Card milledCard = new ReefPirates();
        harness.setLibrary(player2, List.of(milledCard));

        addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0));

        resolveCombat();
        resolveAllTriggers();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(milledCard);
    }

    @Test
    @DisplayName("No mill when Reef Pirates is blocked and deals no damage to a player")
    void noMillWhenBlocked() {
        Card topCard = new ReefPirates();
        harness.setLibrary(player2, List.of(topCard));

        addCreatureReady(player1, new ReefPirates());
        addCreatureReady(player2, new ReefPirates());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(harness.getGameData(), player2,
                List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Defender takes combat damage from unblocked Reef Pirates")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0));

        resolveCombat();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also makes that player mill a card")
    void noncombatDamageToOpponentTriggersMill() {
        Card milledCard = new ReefPirates();
        harness.setLibrary(player2, List.of(milledCard));

        Permanent pirates = addCreatureReady(player1, new ReefPirates());
        attachHermeticStudy(pirates);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(milledCard);
    }

    @Test
    @DisplayName("Damage to its controller does not make Reef Pirates mill a card")
    void damageToControllerDoesNotTriggerMill() {
        Card topCard = new ReefPirates();
        harness.setLibrary(player1, List.of(topCard));

        Permanent pirates = addCreatureReady(player1, new ReefPirates());
        attachHermeticStudy(pirates);

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Two combat damage mills only the top card, not two cards")
    void millsExactlyOneTopCard() {
        Card topCard = new ReefPirates();
        Card nextCard = new ReefPirates();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Noncombat damage to a creature does not mill its controller")
    void damageToCreatureDoesNotTriggerMill() {
        Card topCard = new ReefPirates();
        harness.setLibrary(player2, List.of(topCard));
        Permanent pirates = addCreatureReady(player1, new ReefPirates());
        Permanent victim = addCreatureReady(player2, new ReefPirates());
        attachHermeticStudy(pirates);

        harness.activateAbility(player1, 0, null, victim.getId());
        resolveAllTriggers();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Mill trigger resolves after Reef Pirates returns to hand")
    void millResolvesAfterSourceLeavesBattlefield() {
        Card topCard = new ReefPirates();
        harness.setLibrary(player2, List.of(topCard));
        Permanent pirates = addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, pirates.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pirates);
        assertThat(gd.playerHands.get(player1.getId())).contains(pirates.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    private void attachHermeticStudy(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(creature.getId());
    }
}
