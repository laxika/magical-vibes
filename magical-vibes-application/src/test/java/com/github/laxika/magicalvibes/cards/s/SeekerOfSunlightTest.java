package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSunlight.class, Forest.class, GrizzlyBears.class})
class SeekerOfSunlightTest extends BaseCardTest {

    @Test
    void exploringLandPutsItIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exploringNonLandAddsCounterAndMayPutItIntoGraveyard() {
        Card nonLand = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonLand);
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void activationRequiresSorcerySpeed() {
        addCreatureReady(player1, new SeekerOfSunlight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addExploreMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void addExploreMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
