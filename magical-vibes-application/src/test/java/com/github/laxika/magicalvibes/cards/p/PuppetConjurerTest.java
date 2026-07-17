package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
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
        Permanent conjurer = harness.addToBattlefieldAndReturn(player1, new PuppetConjurer());
        conjurer.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(homunculusTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("At the beginning of your upkeep, a Homunculus is sacrificed")
    void upkeepSacrificesAHomunculus() {
        Permanent conjurer = harness.addToBattlefieldAndReturn(player1, new PuppetConjurer());
        Permanent homunculus = harness.addToBattlefieldAndReturn(player1, homunculusCreature());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice → the lone Homunculus is sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(homunculus.getId()));
        // The Conjurer itself is not a Homunculus, so it is never at risk.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(conjurer.getId()));
    }

    @Test
    @DisplayName("Upkeep with no Homunculus sacrifices nothing")
    void upkeepWithNoHomunculusSacrificesNothing() {
        Permanent conjurer = harness.addToBattlefieldAndReturn(player1, new PuppetConjurer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(conjurer.getId()));
    }
}
