package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlyingCraneTechnique.class, AlpineGrizzly.class, Plains.class})
class FlyingCraneTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps your creatures and grants them flying and double strike")
    void untapsOwnCreaturesAndGrantsKeywords() {
        Permanent ownCreature = addTappedCreature(player1);
        Permanent opponentCreature = addTappedCreature(player2);

        harness.castFromHand(player1, new FlyingCraneTechnique(), "{3}{U}{R}{W}");
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Flying and double strike wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent ownCreature = addTappedCreature(player1);

        harness.castFromHand(player1, new FlyingCraneTechnique(), "{3}{U}{R}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Untapped creatures also gain both keywords, but lands are unaffected")
    void affectsUntappedCreaturesButNotLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        harness.castFromHand(player1, new FlyingCraneTechnique(), "{3}{U}{R}{W}");
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present when the spell resolves gain the keywords")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.castFromHand(player1, new FlyingCraneTechnique(), "{3}{U}{R}{W}");
        Permanent beforeResolution = addTappedCreature(player1);
        harness.passBothPriorities();
        Permanent afterResolution = addTappedCreature(player1);

        assertThat(beforeResolution.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(afterResolution.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new AlpineGrizzly());
        creature.tap();
        return creature;
    }
}
