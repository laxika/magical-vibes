package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborAdherent.class, GrizzlyBears.class, AvatarOfMight.class})
class ArborAdherentTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any chosen color")
    void tapsForOneManaOfAnyColor() {
        Permanent adherent = harness.addToBattlefieldAndReturn(player1, new ArborAdherent());
        adherent.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for mana equal to the greatest toughness among other creatures you control")
    void tapsForGreatestOtherControlledCreatureToughness() {
        Permanent adherent = harness.addToBattlefieldAndReturn(player1, new ArborAdherent());
        adherent.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AvatarOfMight());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability produces no mana when there are no other creatures")
    void producesNoManaWithoutOtherCreatures() {
        Permanent adherent = harness.addToBattlefieldAndReturn(player1, new ArborAdherent());
        adherent.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(adherent.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability uses the greatest current toughness and produces a single color")
    void usesGreatestModifiedToughnessAmongOtherCreatures() {
        Permanent adherent = harness.addToBattlefieldAndReturn(player1, new ArborAdherent());
        adherent.setSummoningSick(false);
        adherent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.addToBattlefield(player1, new ArborAdherent());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ArborAdherent());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(adherent.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
