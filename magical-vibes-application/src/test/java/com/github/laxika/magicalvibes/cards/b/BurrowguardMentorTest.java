package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurrowguardMentor.class, GrizzlyBears.class})
class BurrowguardMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Burrowguard Mentor is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent mentor = addMentorReady(player1);

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only creatures controlled by its controller")
    void countsOnlyControllersCreatures() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    private Permanent addMentorReady(Player player) {
        Permanent permanent = new Permanent(new BurrowguardMentor());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
