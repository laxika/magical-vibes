package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cloudpost.class, Forest.class, Glimmerpost.class})
class CloudpostTest extends BaseCardTest {

    @Test
    @DisplayName("Cloudpost enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new Cloudpost()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Cloudpost").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cloudpost adds one colorless mana for each Locus on the battlefield")
    void addsManaForEachLocusOnTheBattlefield() {
        Permanent cloudpost = harness.addToBattlefieldAndReturn(player1, new Cloudpost());
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player2, new Glimmerpost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cloudpost.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cloudpost counts only Locus lands for its mana ability")
    void countsOnlyLocusLands() {
        harness.addToBattlefield(player1, new Cloudpost());
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
