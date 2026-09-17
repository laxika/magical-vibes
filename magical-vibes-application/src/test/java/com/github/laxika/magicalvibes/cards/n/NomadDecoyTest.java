package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NomadDecoy.class, DwarvenGrunt.class})
class NomadDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability taps a target creature")
    void basicAbilityTapsTargetCreature() {
        Permanent source = addCreatureReady(player1, new NomadDecoy());
        Permanent target = addCreatureReady(player2, new DwarvenGrunt());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability taps two target creatures")
    void thresholdAbilityTapsTwoTargetCreatures() {
        Permanent source = addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new DwarvenGrunt());
        Permanent secondTarget = addCreatureReady(player2, new DwarvenGrunt());
        harness.setGraveyard(player1, List.of(
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(),
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        ));
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability cannot be activated with fewer than seven graveyard cards")
    void thresholdAbilityRequiresSevenGraveyardCards() {
        Permanent source = addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new DwarvenGrunt());
        Permanent secondTarget = addCreatureReady(player2, new DwarvenGrunt());
        harness.setGraveyard(player1, List.of(
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(),
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability requires two different target creatures")
    void thresholdAbilityRequiresDifferentTargets() {
        Permanent source = addCreatureReady(player1, new NomadDecoy());
        Permanent target = addCreatureReady(player2, new DwarvenGrunt());
        harness.setGraveyard(player1, List.of(
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(),
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                target.getId(), target.getId()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability still resolves if the graveyard falls below seven afterward")
    void thresholdIsCheckedWhenActivated() {
        Permanent source = addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new DwarvenGrunt());
        Permanent secondTarget = addCreatureReady(player2, new DwarvenGrunt());
        harness.setGraveyard(player1, List.of(
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(),
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        ));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The threshold ability taps the remaining legal target if one target leaves")
    void thresholdAbilityTapsRemainingLegalTarget() {
        addCreatureReady(player1, new NomadDecoy());
        Permanent firstTarget = addCreatureReady(player2, new DwarvenGrunt());
        Permanent secondTarget = addCreatureReady(player2, new DwarvenGrunt());
        harness.setGraveyard(player1, List.of(
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt(),
                new DwarvenGrunt(), new DwarvenGrunt(), new DwarvenGrunt()
        ));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(
                firstTarget.getId(), secondTarget.getId()
        ));
        gd.playerBattlefields.get(player2.getId()).remove(firstTarget);
        harness.passBothPriorities();

        assertThat(firstTarget.isTapped()).isFalse();
        assertThat(secondTarget.isTapped()).isTrue();
    }
}
