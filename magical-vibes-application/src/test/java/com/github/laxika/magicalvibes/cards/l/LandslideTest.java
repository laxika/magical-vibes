package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Landslide.class, Forest.class, Mountain.class})
class LandslideTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed Mountains determine the damage dealt to the target player")
    void sacrificedMountainsDetermineDamage() {
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Landslide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstMountain.getId(), secondMountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstMountain.getId(), secondMountain.getId()));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("Sacrificed Mountains deal damage to a target planeswalker")
    void sacrificedMountainsDeterminePlaneswalkerDamage() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Landslide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Choosing no Mountains deals no damage and sacrifices nothing")
    void choosingNoMountainsDoesNothing() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Landslide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Having no Mountains requires no choice and deals no damage")
    void havingNoMountainsRequiresNoChoice() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Landslide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Forest");
    }
}
