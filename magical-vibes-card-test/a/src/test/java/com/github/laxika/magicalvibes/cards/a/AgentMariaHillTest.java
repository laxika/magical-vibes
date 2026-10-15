package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HelicarrierStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentMariaHill.class, HelicarrierStrike.class})
class AgentMariaHillTest extends BaseCardTest {

    @Test
    void getsACounterAndDrawsWhenTappedForTeamwork() {
        Permanent maria = addCreatureReady(player1, new AgentMariaHill());
        Card drawnCard = card("Drawn card");
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(teamworkSpell(2)));

        harness.castInstantWithSacrifices(player1, 0, null, List.of(maria.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.isTeamworkCostPaid());
        resolveAllTriggers();

        assertThat(maria.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void mayDeclineTeamworkWithoutTappingOrTriggeringMaria() {
        Permanent maria = addCreatureReady(player1, new AgentMariaHill());
        harness.setHand(player1, List.of(teamworkSpell(2)));

        harness.castInstantWithSacrifices(player1, 0, null, List.of());
        resolveAllTriggers();

        assertThat(maria.isTapped()).isFalse();
        assertThat(maria.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void canPayTeamworkWhileSummoningSickAndTriggerBeforeTheSpellResolves() {
        Permanent maria = harness.addToBattlefieldAndReturn(player1, new AgentMariaHill());
        maria.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new AgentMariaHill());
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        target.tap();
        Card drawnCard = new HelicarrierStrike();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new HelicarrierStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifices(player1, 0, target.getId(), List.of(maria.getId()));

        assertThat(maria.isTapped()).isTrue();
        assertThat(maria.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        harness.passBothPriorities();

        assertThat(maria.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        resolveAllTriggers();
    }

    @Test
    void stillDrawsIfMariaDiesBeforeHerTriggerResolves() {
        Permanent maria = addCreatureReady(player1, new AgentMariaHill());
        Permanent attacker = addCreatureReady(player2, new AgentMariaHill());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        attacker.tap();
        maria.setBlocking(true);
        maria.addBlockingTargetId(attacker.getId());
        Card drawnCard = new HelicarrierStrike();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new HelicarrierStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstantWithSacrifices(player1, 0, attacker.getId(), List.of(maria.getId()));

        harness.setHand(player2, List.of(new HelicarrierStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, maria.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Agent Maria Hill");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void attackingDoesNotTriggerTheTeamworkAbility() {
        Permanent maria = addCreatureReady(player1, new AgentMariaHill());
        Card topCard = new HelicarrierStrike();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(maria.isTapped()).isTrue();
        assertThat(maria.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    private Card teamworkSpell(int requiredPower) {
        Card spell = card("Teamwork test spell");
        spell.setType(CardType.INSTANT);
        spell.setManaCost("{0}");
        spell.addEffect(EffectSlot.SPELL, new TeamworkCost(requiredPower));
        return spell;
    }

    private Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
