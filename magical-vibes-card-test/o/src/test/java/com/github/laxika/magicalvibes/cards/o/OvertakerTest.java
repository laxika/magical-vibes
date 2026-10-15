package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChamberedNautilus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Overtaker.class, Forest.class, ChamberedNautilus.class, IronLance.class})
class OvertakerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card untaps, steals, and grants haste to a target creature")
    void activatesAndResolves() {
        Permanent overtaker = addReadyOvertaker(player1);
        Permanent target = addCreatureReady(player2, new ChamberedNautilus());
        target.tap();
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(overtaker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        addReadyOvertaker(player1);
        Permanent target = addCreatureReady(player2, new ChamberedNautilus());
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyOvertaker(player1);
        Permanent target = addCreatureReady(player2, new ChamberedNautilus());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyOvertaker(player1);
        addCreatureReady(player2, new ChamberedNautilus());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IronLance());
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target itself, paying the tap cost before untapping on resolution")
    void canTargetItself() {
        Permanent overtaker = addReadyOvertaker(player1);
        harness.setHand(player1, List.of(new IronLance()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, overtaker.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(overtaker.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Iron Lance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(overtaker.isTapped()).isFalse();
        assertThat(overtaker.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Overtaker");
        harness.assertNotOnBattlefield(player2, "Overtaker");
    }

    @Test
    @DisplayName("Ability resolves even if Overtaker leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent overtaker = addReadyOvertaker(player1);
        Permanent target = addCreatureReady(player2, new ChamberedNautilus());
        target.tap();
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(overtaker);
        gd.playerGraveyards.get(player1.getId()).add(overtaker.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Chambered Nautilus");
        harness.assertNotOnBattlefield(player2, "Chambered Nautilus");
    }

    @Test
    @DisplayName("Cannot activate while tapped or summoning sick")
    void cannotActivateWhenNotReady() {
        Permanent overtaker = addReadyOvertaker(player1);
        Permanent target = addCreatureReady(player2, new ChamberedNautilus());
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();
        overtaker.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        overtaker.untap();
        overtaker.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Forest");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyOvertaker(Player player) {
        return addCreatureReady(player, new Overtaker());
    }
}
