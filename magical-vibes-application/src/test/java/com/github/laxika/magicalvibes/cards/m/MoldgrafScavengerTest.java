package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoldgrafScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the delirium bonus with fewer than four card types")
    void noDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +3/+0 with four card types in its controller's graveyard")
    void deliriumBonusAtThreshold() {
        setDelirium();
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(0);
    }

    @Test
    @DisplayName("Loses the delirium bonus when its controller's graveyard drops below four card types")
    void losesDeliriumBonusWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        Permanent scavenger = findScavenger();
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(0);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findScavenger() {
        return findPermanent(player1, "Moldgraf Scavenger");
    }
}
