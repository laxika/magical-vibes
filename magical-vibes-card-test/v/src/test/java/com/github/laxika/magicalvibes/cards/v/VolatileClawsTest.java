package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuragandaPetroglyphs;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileClaws.class, GrizzlyBears.class, MuragandaPetroglyphs.class})
class VolatileClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts only creatures you control and grants all creature types")
    void boostsOwnCreaturesAndGrantsAllCreatureTypes() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.ELF)).isTrue();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(opponent, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(own, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Gaining creature types does not remove the bonus for having no abilities")
    void creatureTypeGrantPreservesAbilitylessBonus() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);

        cast();

        assertThat(gqs.hasEffectiveSubtype(gd, own, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither effect")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, original, CardSubtype.ELF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, later, CardSubtype.ELF)).isFalse();
    }

    @Test
    @DisplayName("Can resolve with no creatures and does not affect creatures entering afterward")
    void resolvesWithEmptyBattlefield() {
        cast();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, later, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Multiple resolutions stack their power boosts on every existing own creature")
    void multipleResolutionsBoostAllOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        cast();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.GOBLIN)).isTrue();
    }

    private void cast() {
        harness.castFromHand(player1, new VolatileClaws(), "{2}{R}");
        harness.passBothPriorities();
    }
}
