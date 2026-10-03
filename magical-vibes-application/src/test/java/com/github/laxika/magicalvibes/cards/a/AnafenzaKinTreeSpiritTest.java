package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.m.MassProduction;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnafenzaKinTreeSpirit.class, GrizzlyBears.class, Memnite.class, MassProduction.class,
        DromokaWarrior.class, DragonFodder.class, UltimatePrice.class})
class AnafenzaKinTreeSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature entering bolsters a creature with the least toughness")
    void anotherNontokenCreatureEnteringBolstersLeastToughness() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(memnite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a token entering")
    void doesNotTriggerForTokenEntering() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());

        harness.castFromHand(player1, new MassProduction(), "{5}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotTriggerForOpponentsCreatureEntering() {
        harness.addToBattlefield(player1, new AnafenzaKinTreeSpirit());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when Anafenza enters")
    void doesNotTriggerForSelfEntering() {
        harness.castFromHand(player1, new AnafenzaKinTreeSpirit(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bolster chooses among creatures tied for least toughness, including the entering creature")
    void choosesAmongTiedCreatures() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaKinTreeSpirit());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());

        harness.castFromHand(player1, new DromokaWarrior(), "{1}{W}");
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), entering.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(entering.getId()));

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(anafenza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger still bolsters after Anafenza leaves the battlefield")
    void triggerResolvesWithoutAnafenza() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaKinTreeSpirit());
        harness.castFromHand(player1, new DromokaWarrior(), "{1}{W}");
        harness.passBothPriorities();
        Permanent warrior = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.setHand(player1, List.of(new UltimatePrice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, anafenza.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Anafenza, Kin-Tree Spirit");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bolster chooses at resolution and can put the counter on Anafenza")
    void bolstersAnafenzaAfterEnteringCreatureDies() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaKinTreeSpirit());
        harness.castFromHand(player1, new DromokaWarrior(), "{1}{W}");
        harness.passBothPriorities();
        Permanent warrior = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.setHand(player1, List.of(new UltimatePrice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, warrior.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dromoka Warrior");
        assertThat(anafenza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tokens are eligible for bolster even though they do not trigger Anafenza")
    void canBolsterToken() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaKinTreeSpirit());
        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();

        harness.castFromHand(player1, new DromokaWarrior(), "{1}{W}");
        harness.passBothPriorities();
        Permanent warrior = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                tokens.get(0).getId(), tokens.get(1).getId(), warrior.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(tokens.get(0).getId()));

        assertThat(tokens.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tokens.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(anafenza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
