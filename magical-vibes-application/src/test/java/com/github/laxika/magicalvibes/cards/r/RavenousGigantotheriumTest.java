package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousGigantotherium.class, GrizzlyBears.class, Ornithopter.class, WeldingJar.class})
class RavenousGigantotheriumTest extends BaseCardTest {

    @Test
    @DisplayName("Devour 3 increases the ETB damage total to the creature's power")
    void devourPowerAndDividedDamage() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(firstTarget.getId(), 3, secondTarget.getId(), 3);

        castGigantotherium();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent gigantotherium = findPermanent(player1, "Ravenous Gigantotherium");
        assertThat(gigantotherium.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gigantotherium.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB can target creatures but not noncreature permanents")
    void etbRejectsNoncreatureTarget() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new WeldingJar());

        castGigantotherium();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGigantotherium() {
        harness.setHand(player1, List.of(new RavenousGigantotherium()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
