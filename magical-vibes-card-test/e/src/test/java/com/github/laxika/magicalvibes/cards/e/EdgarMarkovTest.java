package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DuskborneSkymarcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IndulgentAristocrat;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdgarMarkov.class, DuskborneSkymarcher.class, GrizzlyBears.class,
        IndulgentAristocrat.class, InfernalGrasp.class, NamelessInversion.class})
class EdgarMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("Eminence: casting a Vampire spell from the command zone creates a 1/1 black Vampire token")
    void commandZoneEminenceCreatesToken() {
        addToCommandZone(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    @DisplayName("Eminence: casting a Vampire spell from the battlefield creates a 1/1 black Vampire token")
    void battlefieldEminenceCreatesToken() {
        addCreatureReady(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    @DisplayName("Eminence: a non-Vampire spell does not create a token")
    void nonVampireSpellDoesNotCreateToken() {
        addToCommandZone(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on each Vampire you control")
    void attackPutsCounterOnEachVampire() {
        Permanent edgar = addCreatureReady(player1, new EdgarMarkov());
        Permanent skymarcher = addCreatureReady(player1, new DuskborneSkymarcher());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(edgar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(skymarcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eminenceCreatesABlackOneOneVampireCreatureToken() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new IndulgentAristocrat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void eminenceDoesNotFunctionInHandOrGraveyard() {
        harness.setGraveyard(player1, List.of(new EdgarMarkov()));
        harness.setHand(player1, List.of(new IndulgentAristocrat(), new EdgarMarkov()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    void attackAbilityIncludesVampiresThatEnterBeforeItResolves() {
        Permanent edgar = addCreatureReady(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        Permanent lateVampire = harness.enterBattlefieldAndReturn(player1, new IndulgentAristocrat());
        resolveAllTriggers();

        assertThat(edgar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void battlefieldEminenceDoesNothingIfEdgarDiesBeforeResolution() {
        Permanent edgar = addCreatureReady(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new IndulgentAristocrat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, edgar.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Edgar Markov");
        assertThat(findPermanents(player1, "Vampire")).isEmpty();
        harness.assertOnBattlefield(player1, "Indulgent Aristocrat");
    }

    @Test
    void commandZoneEminenceDoesNothingIfEdgarLeavesBeforeResolution() {
        EdgarMarkov edgar = new EdgarMarkov();
        addToCommandZone(player1, edgar);
        harness.setHand(player1, List.of(new IndulgentAristocrat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        gd.playerCommandZones.get(player1.getId()).remove(edgar);
        harness.addToBattlefield(player1, edgar);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    void commandZoneEminenceTriggersForKindredVampireInstant() {
        addToCommandZone(player1, new EdgarMarkov());
        Permanent target = addCreatureReady(player2, new EdgarMarkov());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    void battlefieldEminenceTriggersForKindredVampireInstant() {
        addCreatureReady(player1, new EdgarMarkov());
        Permanent target = addCreatureReady(player2, new EdgarMarkov());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    void opponentsVampireSpellDoesNotTriggerEitherEminenceAbility() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new IndulgentAristocrat()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
        assertThat(findPermanents(player2, "Vampire")).isEmpty();
    }

    @Test
    void castingEdgarFromHandDoesNotTriggerHisOwnEminence() {
        harness.setHand(player1, List.of(new EdgarMarkov()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Edgar Markov");
        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    void attackAbilityStillCountersYourOtherVampiresAfterEdgarDies() {
        Permanent edgar = addCreatureReady(player1, new EdgarMarkov());
        Permanent friendlyVampire = addCreatureReady(player1, new IndulgentAristocrat());
        Permanent opposingVampire = addCreatureReady(player2, new IndulgentAristocrat());
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.castInstant(player2, 0, edgar.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Edgar Markov");
        assertThat(friendlyVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
