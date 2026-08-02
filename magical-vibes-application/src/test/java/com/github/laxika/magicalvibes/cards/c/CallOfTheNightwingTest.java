package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CallOfTheNightwingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Horror token with flying")
    void createsHorrorToken() {
        castCall();
        harness.handleMayAbilityChosen(player1, false);

        Permanent token = findPermanent(player1, "Horror");

        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining cipher puts the spell into the graveyard")
    void decliningCipherGoesToGraveyard() {
        castCall();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Call of the Nightwing");
    }

    @Test
    @DisplayName("Accepting cipher exiles the spell encoded on a creature you control")
    void encodesSpellOnCreature() {
        Permanent encoder = addCreatureReady(player1, new GrizzlyBears());

        castCall();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, encoder.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Call of the Nightwing"));
        harness.assertNotInGraveyard(player1, "Call of the Nightwing");
    }

    private void castCall() {
        harness.setHand(player1, List.of(new CallOfTheNightwing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
