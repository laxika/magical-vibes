package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AgentMariaHill.class)
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
