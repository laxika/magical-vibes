package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
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
        harness.castFromHand(player1, ceremony, "{3}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void covercastIntensifiesOnlyForAnotherSpellWithFiveManaSpent() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.castFromHand(player1, ceremony, "{3}{U}{R}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(3);

        harness.castFromHand(player1, new JacesIngenuity(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(4);
    }

    @Test
    @CardUsed({SummitfestClosingCeremony.class})
    void covercastIntensifiesCeremonyInHandWithoutIntensifyingTheSpellItself() {
        SummitfestClosingCeremony held = new SummitfestClosingCeremony();
        SummitfestClosingCeremony cast = new SummitfestClosingCeremony();
        harness.setHand(player1, List.of(cast, held));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);

        for (int resolutions = 0; resolutions < 3 && !gd.stack.isEmpty(); resolutions++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();

        assertThat(gd.getCardIntensity(held)).isEqualTo(4);
        assertThat(gd.getCardIntensity(cast)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @CardUsed({SummitfestClosingCeremony.class})
    void intensifiedCeremonyProducesMoreManaAfterReturningToHand() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.setGraveyard(player1, List.of(ceremony));
        harness.castFromHand(player1, new SummitfestClosingCeremony(), "{3}{U}{R}");
        for (int resolutions = 0; resolutions < 3 && !gd.stack.isEmpty(); resolutions++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(ceremony)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        gd.playerManaPools.get(player1.getId()).clear();
        SummitfestClosingCeremony drawn = new SummitfestClosingCeremony();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, ceremony, "{3}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @CardUsed({SummitfestClosingCeremony.class})
    void opposingQualifyingSpellDoesNotIntensifyCeremony() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.setGraveyard(player1, List.of(ceremony));
        harness.castFromHand(player2, new SummitfestClosingCeremony(), "{3}{U}{R}");
        for (int resolutions = 0; resolutions < 3 && !gd.stack.isEmpty(); resolutions++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();

        assertThat(gd.getCardIntensity(ceremony)).isEqualTo(3);
    }

    @Test
    @CardUsed({SummitfestClosingCeremony.class, Shock.class})
    void covercastDoesNotPutAnAbilityOnTheStackForAOneManaSpell() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.setGraveyard(player1, List.of(ceremony));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        assertThat(gd.getCardIntensity(ceremony)).isEqualTo(3);
    }
}
