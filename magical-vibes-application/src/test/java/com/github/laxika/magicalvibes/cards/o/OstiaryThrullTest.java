package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OstiaryThrull.class, GrizzlyBears.class, Forest.class})
class OstiaryThrullTest extends BaseCardTest {

    @Test
    void resolvingAbilityTapsTargetCreatureAndPaysTapCost() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(thrull.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnCreature() {
        addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(thrull.isTapped()).isFalse();
    }
}
