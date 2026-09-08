package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenthDistrictLegionnaire.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class TenthDistrictLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Tenth District Legionnaire puts a counter on it and scries 1")
    void targetedSpellPutsCounterAndScries() {
        harness.addToBattlefield(player1, new TenthDistrictLegionnaire());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID legionnaireId = harness.getPermanentId(player1, "Tenth District Legionnaire");
        harness.castInstant(player1, 0, legionnaireId);
        harness.passBothPriorities();

        Permanent legionnaire = findPermanent(player1, "Tenth District Legionnaire");
        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Tenth District Legionnaire")
    void spellTargetingPlayerDoesNotTrigger() {
        harness.addToBattlefield(player1, new TenthDistrictLegionnaire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent legionnaire = findPermanent(player1, "Tenth District Legionnaire");
        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's spell targeting Tenth District Legionnaire does not trigger it")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TenthDistrictLegionnaire());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID legionnaireId = harness.getPermanentId(player1, "Tenth District Legionnaire");
        harness.castInstant(player2, 0, legionnaireId);
        harness.passBothPriorities();

        Permanent legionnaire = findPermanent(player1, "Tenth District Legionnaire");
        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
