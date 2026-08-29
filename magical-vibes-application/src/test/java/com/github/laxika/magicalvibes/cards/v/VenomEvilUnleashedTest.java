package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomEvilUnleashed.class, GrizzlyBears.class, Mountain.class})
class VenomEvilUnleashedTest extends BaseCardTest {

    private void readyAbility() {
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ability puts two +1/+1 counters on target creature and grants deathtouch")
    void abilityBoostsTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Exiling the card is part of the activation cost")
    void exilesSourceAsCost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Venom, Evil Unleashed");
    }

    @Test
    @DisplayName("Deathtouch granted by the ability expires at cleanup")
    void deathtouchExpiresAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Ability requires a creature target")
    void abilityRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyAbility();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
