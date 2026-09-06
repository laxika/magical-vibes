package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SusurianDirgecraft.class, GrizzlyBears.class})
class SusurianDirgecraftTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a nontoken creature")
    void entersAndEachOpponentSacrificesNontokenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDirgecraft();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Station puts charge counters equal to the tapped creature's power on it")
    void stationUsesTappedCreaturePowerAtResolution() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Seven charge counters make it a flying artifact creature")
    void sevenChargeCountersUnlockCreatureAndFlying() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());

        assertThat(gqs.isCreature(gd, dirgecraft)).isFalse();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isFalse();

        dirgecraft.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, dirgecraft)).isTrue();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationRequiresAnotherUntappedCreature() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDirgecraft() {
        harness.setHand(player1, List.of(new SusurianDirgecraft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
