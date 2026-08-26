package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfTheGreatDoor.class, GrizzlyBears.class, Spellbook.class, Forest.class})
class GuardianOfTheGreatDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps four artifacts, creatures, and/or lands as an additional cost")
    void tapsFourEligiblePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), firstCreature.getId(), secondCreature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Guardian of the Great Door");
    }

    @Test
    @DisplayName("Requires exactly four eligible permanents")
    void rejectsTheWrongNumberOfPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfTheGreatDoor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                List.of(artifact.getId(), firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        harness.assertInHand(player1, "Guardian of the Great Door");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
