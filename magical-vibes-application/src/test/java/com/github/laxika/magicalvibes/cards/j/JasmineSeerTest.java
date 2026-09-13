package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.ScentOfBrine;
import com.github.laxika.magicalvibes.cards.s.ScentOfCinder;
import com.github.laxika.magicalvibes.cards.s.SerraAdvocate;
import com.github.laxika.magicalvibes.cards.v.VoiceOfDuty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasmineSeer.class, VoiceOfDuty.class, SerraAdvocate.class, ScentOfCinder.class,
        ScentOfBrine.class})
class JasmineSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each white card in hand")
    void gainsLifeForWhiteCardsInHand() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        harness.setHand(player1, List.of(new VoiceOfDuty(), new SerraAdvocate(), new ScentOfCinder()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores nonwhite cards in hand")
    void ignoresNonwhiteCards() {
        addCreatureReady(player1, new JasmineSeer());
        harness.setHand(player1, List.of(new ScentOfCinder(), new ScentOfBrine()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Counts only white cards in the controller's hand")
    void countsOnlyWhiteCardsInControllerHand() {
        addCreatureReady(player1, new JasmineSeer());
        harness.setHand(player1, List.of(new VoiceOfDuty(), new ScentOfCinder()));
        harness.setHand(player2, List.of(new VoiceOfDuty(), new SerraAdvocate()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new JasmineSeer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
