package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValakutTheMoltenPinnacleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ValakutTheMoltenPinnacle()));

        harness.playLand(player1, 0);

        Permanent valakut = findPermanent(player1, "Valakut, the Molten Pinnacle");
        assertThat(valakut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A sixth Mountain triggers the optional three damage")
    void sixthMountainTriggersDamage() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The fifth Mountain is not enough other Mountains")
    void fifthMountainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(4);
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tapForRedMana() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent valakut = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(valakut.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void addMountains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
    }
}
