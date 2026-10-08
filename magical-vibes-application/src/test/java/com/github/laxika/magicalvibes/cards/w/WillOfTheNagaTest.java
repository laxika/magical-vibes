package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheNaga.class, ArashinCleric.class, Forest.class})
class WillOfTheNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Taps both target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("May tap a single creature")
    void tapsSingleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithoutTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Will of the Naga");
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void delvePaysAllFourGenericManaWithGraveyardCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player1, List.of(new WillOfTheNaga()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, creature.getId(), List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.exiledCards).hasSize(4);
        harness.assertInGraveyard(player1, "Will of the Naga");
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
    }
}
