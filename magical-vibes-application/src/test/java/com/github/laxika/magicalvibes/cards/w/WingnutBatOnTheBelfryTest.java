package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WingnutBatOnTheBelfry.class, GrizzlyBears.class})
class WingnutBatOnTheBelfryTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering lets Wingnut gain flying")
    void anotherCreatureEnteringGrantsFlying() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());

        castGrizzlyBears();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Alliance can grant menace or haste")
    void allianceCanGrantMenaceOrHaste() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());

        castGrizzlyBears();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Wingnut does not trigger from its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new WingnutBatOnTheBelfry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When Wingnut attacks, each other attacking creature gets +1/+0")
    void otherAttackingCreaturesGetBoosted() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent stayHome = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(wingnut.getPowerModifier()).isZero();
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(stayHome.getPowerModifier()).isZero();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
