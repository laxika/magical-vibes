package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfRage.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class OmnathLocusOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 5/5 red and green Elemental token")
    void landfallCreatesElementalToken() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Another Elemental dying lets Omnath deal 3 damage to any target")
    void anotherElementalDeathDealsDamage() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        elemental.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-Elemental creature dying does not trigger Omnath")
    void nonElementalDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Omnath dying triggers its own damage ability")
    void ownDeathDealsDamage() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfRage());
        omnath.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
