package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberethVeteran.class, GrizzlyBears.class})
class EmberethVeteranTest extends BaseCardTest {

    @Test
    void sacrificesAndCreatesYoungHeroRoleAttachedToAnotherCreature() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Embereth Veteran");
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void canAttachYoungHeroRoleToAnOpponentsCreature() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void cannotTargetTheVeteranItself() {
        Permanent veteran = addCreatureReady(player1, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, veteran.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
        harness.assertOnBattlefield(player1, "Embereth Veteran");
    }
}
