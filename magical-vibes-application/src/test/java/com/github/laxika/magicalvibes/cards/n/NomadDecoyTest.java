package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NomadDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability taps a target creature")
    void basicAbilityTapsTargetCreature() {
        addCreatureReady(player1, new NomadDecoy());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability taps two target creatures")
    void thresholdAbilityTapsTwoTargetCreatures() {
        addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        ));
        harness.passBothPriorities();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability cannot be activated with fewer than seven graveyard cards")
    void thresholdAbilityRequiresSevenGraveyardCards() {
        addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }
}
