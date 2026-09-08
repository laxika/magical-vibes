package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodCurdle.class, GrizzlyBears.class, DarksteelMyr.class, Spellbook.class})
class BloodCurdleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature and puts a menace counter on a creature you control")
    void destroysTargetAndPlacesMenaceCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(ownCreature.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(ownCreature.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Chooses one of multiple creatures you control for the menace counter")
    void choosesOwnCreatureForMenaceCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(second.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a menace counter on an illegal-targeted spell")
    void illegalTargetPreventsBothEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MENACE)).isZero();
    }

    @Test
    @DisplayName("Puts the counter on your creature even when the target cannot be destroyed")
    void placesCounterWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(ownCreature.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.setHand(player1, List.of(new BloodCurdle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
