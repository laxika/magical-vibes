package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({SilentClearing.class})
class SilentClearingTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {W} produces white mana and costs 1 life")
    void tapsForWhite() {
        addReadyClearing(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {B} produces black mana and costs 1 life")
    void tapsForBlack() {
        addReadyClearing(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Silent Clearing")
    void sacrificesToDraw() {
        addReadyClearing(player1);
        harness.setLibrary(player1, List.of(new SilentClearing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SilentClearing);
        harness.assertInGraveyard(player1, "Silent Clearing");
    }

    @Test
    @DisplayName("Sacrifice and mana payment occur before the draw ability resolves")
    void sacrificesAsCostBeforeDrawing() {
        addReadyClearing(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SilentClearing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        harness.assertNotOnBattlefield(player1, "Silent Clearing");
        harness.assertInGraveyard(player1, "Silent Clearing");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Silent Clearing");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped Clearing cannot activate either mana ability or the draw ability")
    void tappedLandCannotActivate() {
        Permanent clearing = addReadyClearing(player1);
        clearing.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        harness.assertOnBattlefield(player1, "Silent Clearing");
        harness.assertNotInGraveyard(player1, "Silent Clearing");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability cannot use the same land to pay its mana cost")
    void drawRequiresManaFromAnotherSource() {
        Permanent clearing = addReadyClearing(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(clearing.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Silent Clearing");
        harness.assertNotInGraveyard(player1, "Silent Clearing");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature land can tap for mana on the turn it enters")
    void newlyEnteredLandCanProduceMana() {
        Permanent clearing = addReadyClearing(player1);
        clearing.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(clearing.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyClearing(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SilentClearing());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}

@CardUsed({SilentClearing.class})
class Mh1SilentClearingTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {W} produces white mana and costs 1 life")
    void tapsForWhite() {
        addReadyClearing(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {B} produces black mana and costs 1 life")
    void tapsForBlack() {
        addReadyClearing(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Silent Clearing")
    void sacrificesToDraw() {
        addReadyClearing(player1);
        harness.setLibrary(player1, List.of(new SilentClearing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() instanceof SilentClearing);
        harness.assertInGraveyard(player1, "Silent Clearing");
    }

    private Permanent addReadyClearing(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SilentClearing());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
