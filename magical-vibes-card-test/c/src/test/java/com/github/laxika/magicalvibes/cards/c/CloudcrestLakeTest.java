package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShiftingBorders;
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

@CardUsed({CloudcrestLake.class, ShiftingBorders.class})
class CloudcrestLakeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(lake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and the land stays tapped through the next untap step")
    void tapsForWhiteAndSkipsNextUntap() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.WHITE)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(lake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and the land untaps again on the following turn")
    void tapsForBlueAndSkipsOnlyOneUntapStep() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Colored mana resolves immediately and only this land skips its controller's untap")
    void coloredManaOnlyRestrictsThisLand(int abilityIndex) {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new CloudcrestLake());
        Permanent otherLake = harness.addToBattlefieldAndReturn(player1, new CloudcrestLake());

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(mana(abilityIndex == 1 ? ManaColor.WHITE : ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(lake.isTapped()).isTrue();
        assertThat(otherLake.isTapped()).isTrue();

        advanceToUpkeep(player2);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isTrue();
        assertThat(otherLake.isTapped()).isFalse();

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Changing control does not prevent untapping during the new controller's untap step")
    void coloredManaRestrictionDoesNotFollowNewController(int abilityIndex) {
        Permanent lake = addLake();
        Permanent opposingLake = harness.addToBattlefieldAndReturn(player2, new CloudcrestLake());
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.setHand(player1, List.of(new ShiftingBorders()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(lake.getId(), opposingLake.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(lake);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(lake.isTapped()).isFalse();
    }

    private Permanent addLake() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new CloudcrestLake());
        lake.setSummoningSick(false);
        return lake;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
