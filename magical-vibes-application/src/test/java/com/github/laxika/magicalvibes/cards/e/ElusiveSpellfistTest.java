package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElusiveSpellfist.class, LightningBolt.class, GrizzlyBears.class})
class ElusiveSpellfistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts Elusive Spellfist and makes it unblockable")
    void noncreatureSpellBoostsAndMakesUnblockable() {
        Permanent spellfist = addSpellfist();
        int initialPower = spellfist.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(spellfist.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(spellfist.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Elusive Spellfist")
    void creatureSpellDoesNotTrigger() {
        Permanent spellfist = addSpellfist();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(spellfist.getEffectivePower()).isEqualTo(1);
        assertThat(spellfist.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The boost and unblockability wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent spellfist = addSpellfist();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(spellfist.getEffectivePower()).isEqualTo(2);
        assertThat(spellfist.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spellfist.getEffectivePower()).isEqualTo(1);
        assertThat(spellfist.isCantBeBlocked()).isFalse();
    }

    private Permanent addSpellfist() {
        Permanent spellfist = addCreatureReady(player1, new ElusiveSpellfist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return spellfist;
    }
}
