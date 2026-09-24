package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleSquadron.class, FreshVolunteers.class, Forest.class})
class BattleSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Battle Squadron is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent squadron = addCreatureReady(player1, new BattleSquadron());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle Squadron power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent squadron = addCreatureReady(player1, new BattleSquadron());
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(3);
    }

    @Test
    @DisplayName("Battle Squadron counts only its controller's creatures")
    void countsOnlyControllersCreatures() {
        Permanent squadron = addCreatureReady(player1, new BattleSquadron());
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addToBattlefield(player2, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle Squadron does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        Permanent squadron = addCreatureReady(player1, new BattleSquadron());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battle Squadron updates as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent squadron = addCreatureReady(player1, new BattleSquadron());
        harness.addToBattlefield(player1, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(2);

        harness.addToBattlefield(player1, new FreshVolunteers());
        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fresh Volunteers"));
        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);
    }
}
