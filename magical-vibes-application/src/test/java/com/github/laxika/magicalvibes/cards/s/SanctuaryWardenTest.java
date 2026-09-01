package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctuaryWarden.class, GrizzlyBears.class})
class SanctuaryWardenTest extends BaseCardTest {

    @Test
    void entersWithShieldCountersAndMayDrawAndCreateCitizen() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent warden = castWarden();

        assertThat(warden.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(warden.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Citizen");
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getColors())
                            .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
                });
    }

    @Test
    void choosingPermanentAndCounterTypeRemovesOnlyTheChosenCounter() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        otherCreature.setCounterCount(CounterType.SHIELD, 1);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        Permanent warden = castWarden();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, otherCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)
                .options()).containsExactlyInAnyOrder("+1/+1 counters", "shield counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(warden.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void decliningDoesNotRemoveCounterDrawOrCreateToken() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent warden = castWarden();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(warden.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    void attackTriggerMayRemoveCounterAndCreateCitizen() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent warden = addCreatureReady(player1, new SanctuaryWarden());
        warden.setCounterCount(CounterType.SHIELD, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(warden.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private Permanent castWarden() {
        harness.setHand(player1, List.of(new SanctuaryWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Sanctuary Warden"))
                .findFirst()
                .orElseThrow();
    }
}
