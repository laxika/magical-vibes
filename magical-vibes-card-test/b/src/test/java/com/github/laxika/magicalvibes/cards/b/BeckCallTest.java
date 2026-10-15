package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.f.Flashfreeze;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeckCall.class, KraulWarrior.class, Flashfreeze.class})
class BeckCallTest extends BaseCardTest {

    private static final int BECK = 0;
    private static final int CALL = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Beck lets you draw when any creature enters")
    void beckDrawsForAnyCreatureEntering() {
        harness.setLibrary(player1, List.of(new KraulWarrior(), new KraulWarrior()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, BECK);

        harness.setHand(player1, List.of(new KraulWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Call creates four flying Bird tokens")
    void callCreatesBirds() {
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, CALL);

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .hasSize(4)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.BIRD);
                    assertThat(permanent.getCard().getColors()).containsExactly(CardColor.WHITE);
                    assertThat(permanent.hasKeyword(Keyword.FLYING)).isTrue();
                    assertThat(permanent.getEffectivePower()).isEqualTo(1);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Fused Beck and Call triggers for each Bird token")
    void fusedTriggersForCallTokens() {
        harness.setLibrary(player1, List.of(
                new KraulWarrior(), new KraulWarrior(), new KraulWarrior(), new KraulWarrior()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of());
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void fuseRequiresTwoBlueMana() {
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void beckDrawsForOpponentsCreature() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new KraulWarrior()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, BECK);

        harness.enterBattlefieldAndReturn(player2, new KraulWarrior());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kraul Warrior");
        assertThat(harness.getGameData().playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void decliningDrawDoesNotPreventLaterDraw() {
        harness.setLibrary(player1, List.of(new KraulWarrior()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, BECK);

        harness.enterBattlefieldAndReturn(player1, new KraulWarrior());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new KraulWarrior());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Kraul Warrior");
    }

    @Test
    void beckStopsTriggeringAfterTheTurnEnds() {
        harness.setLibrary(player1, List.of(new KraulWarrior(), new KraulWarrior()));
        harness.setLibrary(player2, List.of(new KraulWarrior(), new KraulWarrior()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, BECK);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player2, new KraulWarrior());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({BeckCall.class, Flashfreeze.class})
    void callCannotBeTargetedByFlashfreeze() {
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, CALL, List.of());

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        var callId = harness.getGameData().stack.getLast().getCard().getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, callId))
                .isInstanceOf(IllegalStateException.class);
    }
}
