package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;




@CardUsed({SunbakedCanyon.class, AlpineGuide.class})
class SunbakedCanyonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sunbaked Canyon for red mana costs 1 life")
    void tapsForRedMana() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new SunbakedCanyon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(canyon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Sunbaked Canyon for white mana costs 1 life")
    void tapsForWhiteMana() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new SunbakedCanyon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(canyon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {1}, tapping, and sacrificing Sunbaked Canyon draws a card")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new SunbakedCanyon());
        harness.setLibrary(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SunbakedCanyon);
        harness.assertInGraveyard(player1, "Sunbaked Canyon");
    }
}

@CardUsed({SunbakedCanyon.class, AlpineGuide.class})
class Mh1SunbakedCanyonTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {R} produces red mana and costs 1 life")
    void tapsForRed() {
        addReadyCanyon(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {W} produces white mana and costs 1 life")
    void tapsForWhite() {
        addReadyCanyon(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Sunbaked Canyon")
    void sacrificesToDraw() {
        addReadyCanyon(player1);
        harness.setLibrary(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() instanceof SunbakedCanyon);
        harness.assertInGraveyard(player1, "Sunbaked Canyon");
    }

    @Test
    @DisplayName("Life and tap costs are paid before choosing a mana color")
    void paysManaAbilityCostsBeforeColorChoice() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new SunbakedCanyon());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 9);
        assertThat(canyon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "RED");

        harness.assertLife(player1, 9);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Sacrifice and mana costs are paid before the draw ability resolves")
    void sacrificesBeforeDrawing() {
        harness.addToBattlefield(player1, new SunbakedCanyon());
        AlpineGuide drawnCard = new AlpineGuide();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Sunbaked Canyon");
        harness.assertInGraveyard(player1, "Sunbaked Canyon");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability cannot be activated without mana")
    void cannotSacrificeWithoutMana() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new SunbakedCanyon());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(canyon.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sunbaked Canyon");
        harness.assertNotInGraveyard(player1, "Sunbaked Canyon");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Canyon cannot activate either ability")
    void cannotActivateWhileTapped() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new SunbakedCanyon());
        canyon.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Sunbaked Canyon");
        harness.assertNotInGraveyard(player1, "Sunbaked Canyon");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyCanyon(Player player) {
        Permanent permanent = addCreatureReady(player, new SunbakedCanyon());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
