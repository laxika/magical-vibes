package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpecimenFreighter.class, GrizzlyBears.class, LumenClassFrigate.class})
class SpecimenFreighterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to two non-Spacecraft creatures to their owners' hands")
    void etbReturnsTwoNonSpacecraftCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFreighter(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(ownCreature.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature, opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("ETB cannot target an animated Spacecraft creature")
    void etbCannotTargetSpacecraftCreature() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player2, new LumenClassFrigate());
        spacecraft.setCounterCount(CounterType.CHARGE, 12);

        harness.setHand(player1, List.of(new SpecimenFreighter()));
        addFreighterMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(spacecraft.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Spacecraft creature");
    }

    @Test
    @DisplayName("Station uses the tapped creature's power and unlocks flying at nine charge counters")
    void stationUsesCreaturePowerAndUnlocksFlying() {
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(freighter), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(freighter.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        freighter.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking mills four cards from the defending player's library")
    void attackingMillsDefendingPlayer() {
        Permanent freighter = addCreatureReady(player1, new SpecimenFreighter());
        freighter.setCounterCount(CounterType.CHARGE, 9);
        List<Card> cards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, cards);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
    }

    private void castFreighter(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SpecimenFreighter()));
        addFreighterMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addFreighterMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
