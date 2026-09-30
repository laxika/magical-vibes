package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummitfestClosingCeremony.class, JacesIngenuity.class, Shock.class})
class SummitfestClosingCeremonyTest extends BaseCardTest {

    @Test
    void startsAtIntensityThreeAndDrawsACard() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        Shock drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(ceremony));
        addCeremonyMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void covercastIntensifiesOnlyForAnotherSpellWithFiveManaSpent() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.setHand(player1, List.of(ceremony));
        addCeremonyMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(3);

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(4);
    }

    private void addCeremonyMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
