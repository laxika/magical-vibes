package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SkyclaveRelic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodchiefsThirst.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class,
        IntoTheRoil.class, Ornithopter.class, SkyclaveRelic.class})
class BloodchiefsThirstTest extends BaseCardTest {

    @Test
    void destroysCreatureWithManaValueTwoOrLessWithoutKicker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureWithManaValueAboveTwoWithoutKicker() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    void kickedDestroysCreatureWithAnyManaValue() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void kickedDestroysPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }

    @Test
    void destroysZeroManaArtifactCreatureWithoutKicker() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, ornithopter.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void canDestroyControllersOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetHighManaValuePlaneswalkerWithoutKicker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, chandra.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");

        harness.assertOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    void kickerDoesNotAllowTargetingNoncreatureNonplaneswalkerArtifact() {
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new SkyclaveRelic());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, relic.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Skyclave Relic");
        harness.assertInHand(player1, "Bloodchief's Thirst");
    }

    @Test
    void kickerRequiresAdditionalBlackMana() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Bloodchief's Thirst");
    }

    @Test
    void doesNotDestroyTargetReturnedToHandInResponse() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bloodchief's Thirst");
    }
}
