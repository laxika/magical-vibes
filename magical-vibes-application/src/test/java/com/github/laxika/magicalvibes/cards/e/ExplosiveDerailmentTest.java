package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveDerailment.class, FountainOfYouth.class, GrizzlyBears.class})
class ExplosiveDerailmentTest extends BaseCardTest {

    @Test
    @DisplayName("The damage mode deals 4 damage to a target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The artifact mode destroys a target artifact")
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(new int[]{1}, List.of(harness.getPermanentId(player2, "Fountain of Youth")), 2);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Spree resolves both modes and charges both additional costs")
    void resolvesBothModes() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(new int[]{0, 1}, List.of(
                creature.getId(),
                harness.getPermanentId(player2, "Fountain of Youth")), 4);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The damage mode rejects an artifact target")
    void rejectsArtifactForDamageMode() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(artifact.getId()), 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The artifact mode rejects a creature target")
    void rejectsCreatureForArtifactMode() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId()), 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int additionalColorlessMana) {
        harness.setHand(player1, List.of(new ExplosiveDerailment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, additionalColorlessMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
