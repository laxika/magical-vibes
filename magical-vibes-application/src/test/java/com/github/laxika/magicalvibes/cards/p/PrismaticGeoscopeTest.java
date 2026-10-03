package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismaticGeoscope.class, Forest.class, Island.class, Mountain.class, Plains.class})
class PrismaticGeoscopeTest extends BaseCardTest {

    @Test
    @DisplayName("Prismatic Geoscope enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new PrismaticGeoscope()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Prismatic Geoscope").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds one mana for each distinct basic land type in any combination")
    void addsManaForDistinctBasicLandTypes() {
        Permanent geoscope = harness.addToBattlefieldAndReturn(player1, new PrismaticGeoscope());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(geoscope.isTapped()).isTrue();
    }
}
