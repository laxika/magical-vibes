package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.c.CultivatorsCaravan;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({KatsumasaTheAnimator.class, Arachnoid.class, ConjurersBauble.class,
        CultivatorsCaravan.class})
class KatsumasaTheAnimatorTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a non-Vehicle artifact into a 1/1 artifact creature with flying")
    void animatesNonVehicleArtifact() {
        addKatsumasa();
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

        activateAnimation(bauble);

        assertThat(gqs.isArtifact(gd, bauble)).isTrue();
        assertThat(gqs.isCreature(gd, bauble)).isTrue();
        assertThat(gqs.hasKeyword(gd, bauble, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bauble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bauble)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animates a Vehicle without changing its printed base power and toughness")
    void animatesVehicleWithoutChangingBasePowerToughness() {
        addKatsumasa();
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new CultivatorsCaravan());

        activateAnimation(caravan);

        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(gqs.hasKeyword(gd, caravan, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, caravan)).contains(CardSubtype.VEHICLE);
        assertThat(gqs.getEffectivePower(gd, caravan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, caravan)).isEqualTo(5);
    }

    @Test
    @DisplayName("The animation ability only targets a noncreature artifact you control")
    void animationAbilityRejectsIllegalTargets() {
        addKatsumasa();
        Permanent opponentBauble = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBauble.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact you control");
    }

    @Test
    @DisplayName("The upkeep trigger puts counters on up to three targeted noncreature artifacts")
    void upkeepTriggerPutsCountersOnTargets() {
        addKatsumasa();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());

        advanceToUpkeep(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep trigger cannot target an artifact creature")
    void upkeepTriggerRejectsArtifactCreature() {
        addKatsumasa();
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());

        advanceToUpkeep(player1);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(artifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addKatsumasa() {
        return harness.addToBattlefieldAndReturn(player1, new KatsumasaTheAnimator());
    }

    private void activateAnimation(Permanent target) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
