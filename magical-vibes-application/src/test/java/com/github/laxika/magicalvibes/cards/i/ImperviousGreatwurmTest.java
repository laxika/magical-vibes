package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperviousGreatwurm.class, GrizzlyBears.class})
class ImperviousGreatwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Indestructible lets Impervious Greatwurm survive lethal damage")
    void indestructibleSurvivesLethalDamage() {
        Permanent greatwurm = addCreatureReady(player1, new ImperviousGreatwurm());
        greatwurm.setMarkedDamage(16);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(greatwurm);
    }

    @Test
    @DisplayName("Convoke taps creatures to pay for Impervious Greatwurm")
    void convokeTapsCreaturesToPayForIt() {
        Permanent firstHelper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondHelper = addCreatureReady(player1, new GrizzlyBears());
        Permanent thirdHelper = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstHelper.getId(), secondHelper.getId(), thirdHelper.getId()));

        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
        assertThat(thirdHelper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof ImperviousGreatwurm);
    }
}
