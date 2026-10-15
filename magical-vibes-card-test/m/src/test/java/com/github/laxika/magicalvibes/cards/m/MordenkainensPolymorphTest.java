package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MordenkainensPolymorph.class, NeverwinterDryad.class, SpareDagger.class})
class MordenkainensPolymorphTest extends BaseCardTest {

    @Test
    void transformsTargetCreatureIntoDragonWithBaseFourFourAndFlying() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());

        castMordenkainensPolymorph(dryad.getId());

        assertThat(dryad.getEffectivePower()).isEqualTo(4);
        assertThat(dryad.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(dryad, CardSubtype.DRAGON)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(dryad, CardSubtype.DRYAD)).isFalse();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());

        castMordenkainensPolymorph(dryad.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dryad.getEffectivePower()).isEqualTo(1);
        assertThat(dryad.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(dryad, CardSubtype.DRYAD)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(dryad, CardSubtype.DRAGON)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player2, new SpareDagger());
        harness.addToBattlefield(player1, new NeverwinterDryad());
        harness.setHand(player1, List.of(new MordenkainensPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID daggerId = dagger.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, daggerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void baseStatsPreserveCountersAndEquipmentBonuses() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        dryad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new SpareDagger());
        dagger.setAttachedTo(dryad.getId());

        castMordenkainensPolymorph(dryad.getId());

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isTrue();
    }

    @Test
    void retainsOriginalActivatedAbilityAndDoesNotAffectOtherCreatures() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());

        castMordenkainensPolymorph(dryad.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(other, CardSubtype.DRYAD)).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Neverwinter Dryad");
        harness.assertInGraveyard(player1, "Neverwinter Dryad");
    }

    private void castMordenkainensPolymorph(UUID targetId) {
        harness.setHand(player1, List.of(new MordenkainensPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
