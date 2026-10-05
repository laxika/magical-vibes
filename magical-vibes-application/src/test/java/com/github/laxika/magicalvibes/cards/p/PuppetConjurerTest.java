package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PuppetConjurer.class})
class PuppetConjurerTest extends BaseCardTest {

    // {U}, {T}: Create a 0/1 blue Homunculus artifact creature token.
    // At the beginning of your upkeep, sacrifice a Homunculus.

    private static Card homunculusCreature() {
        Card card = new Card();
        card.setName("Test Homunculus");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLUE);
        card.setSubtypes(List.of(CardSubtype.HOMUNCULUS));
        card.setPower(0);
        card.setToughness(1);
        return card;
    }

    private long homunculusTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.HOMUNCULUS))
                .filter(p -> p.getCard().getColor() == CardColor.BLUE)
                .filter(p -> p.getCard().getPower() == 0 && p.getCard().getToughness() == 1)
                .filter(p -> p.getCard().hasType(CardType.ARTIFACT))
                .count();
    }

    @Test
    @DisplayName("Activated ability creates a 0/1 blue Homunculus artifact creature token")
    void activatesToCreateHomunculusToken() {
        addCreatureReady(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(homunculusTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("At the beginning of your upkeep, a Homunculus is sacrificed")
    void upkeepSacrificesAHomunculus() {
        harness.addToBattlefield(player1, new PuppetConjurer());
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, homunculusCreature());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice → the lone Homunculus is sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(homunculus.getId()));
        // The Conjurer itself is not a Homunculus, so it is never at risk.
        harness.assertOnBattlefield(player1, "Puppet Conjurer");
    }

    @Test
    @DisplayName("Upkeep with no Homunculus sacrifices nothing")
    void upkeepWithNoHomunculusSacrificesNothing() {
        harness.addToBattlefield(player1, new PuppetConjurer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Puppet Conjurer");
    }

    @Test
    @DisplayName("Activation pays the tap cost before the token is created")
    void activationTapsConjurerAndUsesTheStack() {
        Permanent conjurer = addCreatureReady(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(conjurer.isTapped()).isTrue();
        assertThat(homunculusTokens()).isZero();
        harness.passBothPriorities();
        assertThat(homunculusTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Conjurer cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(homunculusTokens()).isZero();
    }

    @Test
    @DisplayName("The activation requires blue mana")
    void colorlessManaCannotPayActivationCost() {
        Permanent conjurer = addCreatureReady(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(conjurer.isTapped()).isFalse();
        assertThat(homunculusTokens()).isZero();
    }

    @Test
    @DisplayName("The controller chooses exactly one of multiple Homunculi to sacrifice")
    void choosesOneHomunculusAtResolution() {
        Permanent conjurer = addCreatureReady(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        conjurer.setTapped(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        List<Permanent> tokens = findPermanents(player1, "Homunculus");
        assertThat(tokens).hasSize(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(tokens.get(1).getId()));

        assertThat(findPermanents(player1, "Homunculus"))
                .extracting(Permanent::getId).containsExactly(tokens.get(0).getId());
        harness.assertOnBattlefield(player1, "Puppet Conjurer");
    }

    @Test
    @DisplayName("The sacrifice ability does not trigger on the opponent's upkeep")
    void opponentUpkeepDoesNotSacrificeHomunculus() {
        addCreatureReady(player1, new PuppetConjurer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(homunculusTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Homunculus created in response to the upkeep trigger must be sacrificed")
    void tokenCreatedInResponseIsSacrificed() {
        harness.addToBattlefield(player1, new PuppetConjurer());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(homunculusTokens()).isEqualTo(1);
        resolveAllTriggers();

        assertThat(homunculusTokens()).isZero();
        harness.assertOnBattlefield(player1, "Puppet Conjurer");
    }

    @Test
    @DisplayName("The upkeep sacrifice cannot take an opponent's Homunculus")
    void cannotSacrificeOpponentsHomunculus() {
        harness.addToBattlefield(player1, new PuppetConjurer());
        addCreatureReady(player2, new PuppetConjurer());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Homunculus")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Puppet Conjurer");
    }
}
