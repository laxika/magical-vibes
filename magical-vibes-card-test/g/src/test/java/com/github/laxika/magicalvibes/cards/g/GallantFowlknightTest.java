package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CennsHeir;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallantFowlknight.class, CennsHeir.class, GrizzlyBears.class, RayOfCommand.class})
class GallantFowlknightTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures you control and gives Kithkin first strike")
    void boostsOwnCreaturesAndGrantsKithkinFirstStrike() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new CennsHeir());
        Permanent nonKithkin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GallantFowlknight(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Gallant Fowlknight");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonKithkin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kithkin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKithkin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and first strike last until end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.castFromHand(player1, new GallantFowlknight(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Gallant Fowlknight");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A source stolen before its trigger resolves does not gain first strike")
    void stolenSourceDoesNotGainFirstStrike() {
        Permanent ownKithkin = harness.addToBattlefieldAndReturn(player1, new GallantFowlknight());
        harness.castFromHand(player1, new GallantFowlknight(), "{3}{W}");
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != ownKithkin)
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(source);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownKithkin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownKithkin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger affects creatures present at resolution but not later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.castFromHand(player1, new GallantFowlknight(), "{3}{W}");
        harness.passBothPriorities();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GallantFowlknight());
        Permanent opposingKithkin = harness.addToBattlefieldAndReturn(player2, new GallantFowlknight());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GallantFowlknight());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingKithkin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingKithkin, Keyword.FIRST_STRIKE)).isFalse();
    }
}
