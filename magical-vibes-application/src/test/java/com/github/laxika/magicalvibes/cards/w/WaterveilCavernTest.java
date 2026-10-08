package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterveilCavern.class, Confiscate.class})
class WaterveilCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(cavern.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and the land stays tapped through the next untap step")
    void tapsForBlueAndSkipsNextUntap() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(cavern.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and the land untaps again on the following turn")
    void tapsForBlackAndSkipsOnlyOneUntapStep() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(cavern.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(cavern.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's untap step does not consume the colored mana restriction")
    void opponentsUntapDoesNotConsumeRestriction() {
        Permanent cavern = addCavern();
        harness.activateAbility(player1, 0, 1, null, null);

        harness.performUntapStep(player2);
        assertThat(cavern.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(cavern.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(cavern.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A new controller can untap the land during their own untap step")
    void newControllerUntapsNormallyAfterColoredManaActivation() {
        Permanent cavern = addCavern();
        harness.activateAbility(player1, 0, 2, null, null);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, cavern.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(cavern);
        assertThat(cavern.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(cavern.isTapped()).isFalse();
    }

    private Permanent addCavern() {
        return harness.addToBattlefieldAndReturn(player1, new WaterveilCavern());
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
