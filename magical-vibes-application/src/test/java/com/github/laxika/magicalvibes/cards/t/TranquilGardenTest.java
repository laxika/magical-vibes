package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilGarden.class, Confiscate.class})
class TranquilGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent garden = addGarden();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(garden.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(garden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for green adds {G} and the land stays tapped through the next untap step")
    void tapsForGreenAndSkipsNextUntap() {
        Permanent garden = addGarden();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(garden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and the land untaps again on the following turn")
    void tapsForWhiteAndSkipsOnlyOneUntapStep() {
        Permanent garden = addGarden();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.WHITE)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(garden.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(garden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A colored activation keeps only the activated land tapped")
    void coloredActivationOnlySkipsTheActivatedLand() {
        Permanent activatedGarden = addGarden();
        Permanent otherGarden = addGarden();
        otherGarden.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        advanceToUpkeep(player1);

        assertThat(activatedGarden.isTapped()).isTrue();
        assertThat(otherGarden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The opponent's untap step does not consume the colored mana restriction")
    void opponentsUntapDoesNotConsumeRestriction() {
        Permanent garden = addGarden();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        advanceToUpkeep(player2);
        assertThat(garden.isTapped()).isTrue();
        advanceToUpkeep(player1);
        assertThat(garden.isTapped()).isTrue();
        advanceToUpkeep(player1);
        assertThat(garden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A new controller can untap the land before the activating player's next untap step")
    void newControllerCanUntapBeforeActivatorsNextUntap() {
        Permanent garden = addGarden();
        harness.activateAbility(player1, 0, 1, null, null);

        stealGarden(garden);
        advanceToUpkeep(player2);

        assertThat(garden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The restriction expires at the activating player's untap step after control changes")
    void restrictionExpiresAtActivatorsUntapAfterControlChanges() {
        Permanent garden = addGarden();
        harness.activateAbility(player1, 0, 2, null, null);

        stealGarden(garden);
        advanceToUpkeep(player1);
        assertThat(garden.isTapped()).isTrue();
        advanceToUpkeep(player2);

        assertThat(garden.isTapped()).isFalse();
    }

    private void stealGarden(Permanent garden) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, garden.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(garden);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(garden);
    }

    private Permanent addGarden() {
        Permanent garden = harness.addToBattlefieldAndReturn(player1, new TranquilGarden());
        garden.setSummoningSick(false);
        return garden;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
