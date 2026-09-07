package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({HyldasCrownOfWinter.class, GrizzlyBears.class, Forest.class})
class HyldasCrownOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("The tap ability costs no mana during its controller's turn")
    void tapAbilityIsReducedDuringControllerTurn() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new HyldasCrownOfWinter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(crown.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability costs one mana during an opponent's turn")
    void tapAbilityIsNotReducedDuringOpponentsTurn() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new HyldasCrownOfWinter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(crown.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the crown draws for each tapped opposing creature")
    void sacrificeAbilityDrawsForTappedOpposingCreatures() {
        harness.addToBattlefield(player1, new HyldasCrownOfWinter());
        Permanent tappedCreature = addCreatureReady(player2, new GrizzlyBears());
        tappedCreature.tap();
        Permanent secondTappedCreature = addCreatureReady(player2, new GrizzlyBears());
        secondTappedCreature.tap();
        addCreatureReady(player2, new GrizzlyBears());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedLand.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Hylda's Crown of Winter");
    }
}
