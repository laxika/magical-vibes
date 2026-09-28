package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorWaukiTheYounger.class, Divination.class, Shock.class, HillGiant.class})
class TorWaukiTheYoungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery triggers 2 damage to any target")
    void castingInstantOrSorceryTriggersDamage() {
        addTorWauki();
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another source's noncombat damage gets +1, but Tor Wauki's trigger does not")
    void boostsAnotherSourceDamageOnly() {
        addTorWauki();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An opponent's noncombat damage is not increased")
    void doesNotBoostOpponentsSources() {
        addTorWauki();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The trigger can deal its damage to a permanent")
    void triggerCanTargetPermanent() {
        addTorWauki();
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        chooseTriggerTarget(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private void addTorWauki() {
        harness.addToBattlefield(player1, new TorWaukiTheYounger());
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
    }
}
