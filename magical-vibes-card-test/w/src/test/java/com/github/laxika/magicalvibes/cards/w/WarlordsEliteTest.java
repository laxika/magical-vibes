package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarlordsElite.class, Forest.class, GrizzlyBears.class, Spellbook.class,
        EnergyRefractor.class, YotianFrontliner.class})
class WarlordsEliteTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Taps two artifacts, creatures, and/or lands as an additional cost")
    void tapsTwoEligiblePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0, List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Warlord's Elite");
    }

    @Test
    @DisplayName("Requires exactly two eligible permanents")
    void rejectsTheWrongNumberOfPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        harness.assertInHand(player1, "Warlord's Elite");
    }

    @Test
    void canTapTwoLandsAndPaysBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Warlord's Elite");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Warlord's Elite");
    }

    @Test
    void canTapSummoningSickArtifactCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Warlord's Elite");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotOmitPartOfTheAdditionalCost(int selectedCount) {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                selectedCount == 0 ? List.of() : List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Warlord's Elite");
    }

    @Test
    void cannotCountAnArtifactCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Warlord's Elite");
    }

    @Test
    void cannotTapAnOpponentsPermanent() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(own.getId(), opponents.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(own.isTapped()).isFalse();
        assertThat(opponents.isTapped()).isFalse();
        harness.assertInHand(player1, "Warlord's Elite");
    }

    @Test
    void cannotTapAnAlreadyTappedPermanent() {
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        tapped.tap();
        harness.setHand(player1, List.of(new WarlordsElite()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(untapped.getId(), tapped.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(untapped.isTapped()).isFalse();
        assertThat(tapped.isTapped()).isTrue();
        harness.assertInHand(player1, "Warlord's Elite");
    }
}
