package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvolvedSleeper;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NemataPrimevalWarden.class, GrizzlyBears.class, Shock.class, EvolvedSleeper.class})
class NemataPrimevalWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's dying creature and creates a Saproling")
    void exilesOpponentsDyingCreatureAndCreatesSaproling() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Saproling when an opponent's Saproling token would die")
    void createsSaprolingForDyingOpponentToken() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        Permanent token = addCreatureReady(player2, createSaprolingToken());
        token.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Saproling");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Does not replace a creature controlled by Nemata's controller")
    void doesNotReplaceOwnDyingCreature() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts Nemata")
    void sacrificingSaprolingBoostsNemata() {
        Permanent nemata = addCreatureReady(player1, new NemataPrimevalWarden());
        addCreatureReady(player1, createSaprolingToken());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nemata.getEffectivePower()).isEqualTo(5);
        assertThat(nemata.getEffectiveToughness()).isEqualTo(6);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two Saprolings draws a card")
    void sacrificingTwoSaprolingsDrawsCard() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        addCreatureReady(player1, createSaprolingToken());
        addCreatureReady(player1, createSaprolingToken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("The draw ability requires two Saprolings")
    void drawAbilityRequiresTwoSaprolings() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        addCreatureReady(player1, createSaprolingToken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Saproling creation waits for its triggered ability to resolve")
    void tokenCreationUsesStack() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        Permanent sleeper = addCreatureReady(player2, new EvolvedSleeper());
        sleeper.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.findExiledCard(sleeper.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Nemata without abilities does not exile dying opponents' creatures")
    void abilityLossDisablesReplacement() {
        Permanent nemata = addCreatureReady(player1, new NemataPrimevalWarden());
        nemata.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent sleeper = addCreatureReady(player2, new EvolvedSleeper());
        sleeper.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Evolved Sleeper");
        assertThat(gd.findExiledCard(sleeper.getCard().getId())).isNull();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Nemata still replaces opposing deaths when it dies simultaneously")
    void simultaneousDeathStillCreatesToken() {
        Permanent nemata = addCreatureReady(player1, new NemataPrimevalWarden());
        Permanent sleeper = addCreatureReady(player2, new EvolvedSleeper());
        nemata.setMarkedDamage(4);
        sleeper.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nemata, Primeval Warden");
        assertThat(gd.findExiledCard(sleeper.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The pump cost cannot sacrifice an opponent's Saproling")
    void cannotSacrificeOpponentsSaproling() {
        addCreatureReady(player1, new NemataPrimevalWarden());
        addCreatureReady(player2, createSaprolingToken());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    private Card createSaprolingToken() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setManaCost("{0}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        card.setToken(true);
        return card;
    }
}
