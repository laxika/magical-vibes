package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VialSmasherGleefulGrenadier.class, DauthiMercenary.class, GrizzlyBears.class})
class VialSmasherGleefulGrenadierTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a chosen opponent when an outlaw enters under your control")
    void dealsDamageWhenOutlawEnters() {
        harness.addToBattlefield(player1, new VialSmasherGleefulGrenadier());
        harness.setHand(player1, List.of(new DauthiMercenary()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger when a non-outlaw creature enters under your control")
    void doesNotTriggerForNonOutlaw() {
        harness.addToBattlefield(player1, new VialSmasherGleefulGrenadier());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for its own entry despite being a Mercenary")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new VialSmasherGleefulGrenadier()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vial Smasher, Gleeful Grenadier");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's outlaw enters")
    void doesNotTriggerForOpponentsOutlaw() {
        harness.addToBattlefield(player1, new VialSmasherGleefulGrenadier());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new VialSmasherGleefulGrenadier()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Vial Smasher, Gleeful Grenadier");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
