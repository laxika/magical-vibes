package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RibCageSpider;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChimericIdol.class, RhysticCave.class, RibCageSpider.class})
class ChimericIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Taps controlled lands and becomes a 3/3 Turtle artifact creature")
    void activatesByTappingControlledLandsAndAnimating() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new ChimericIdol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new RibCageSpider());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(idol), null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(nonland.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
        assertThat(idol.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
        assertThat(gqs.isArtifact(idol)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, idol, CardSubtype.TURTLE)).isTrue();
    }

    @Test
    @DisplayName("Chimeric Idol stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new ChimericIdol());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, idol)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idol)).isFalse();
        assertThat(gqs.isArtifact(idol)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, idol, CardSubtype.TURTLE)).isFalse();
    }

    @Test
    @DisplayName("Can activate while already tapped")
    void activatesWhileAlreadyTapped() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new ChimericIdol());
        idol.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(idol.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
    }
}
