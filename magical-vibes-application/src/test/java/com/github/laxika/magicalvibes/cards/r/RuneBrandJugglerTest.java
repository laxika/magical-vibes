package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuneBrandJuggler.class})
class RuneBrandJugglerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB suspects up to one target creature you control")
    void entersAndSuspectsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());

        castJuggler(bear.getId());

        assertThat(bear.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("ETB may choose no target")
    void entersWithoutSuspectingWhenNoTargetIsChosen() {
        castJuggler(null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.isSuspected());
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        harness.setHand(player1, List.of(new RuneBrandJuggler()));
        addCastMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a suspected creature gives a target creature -5/-5")
    void sacrificesSuspectedCreatureAndWeakensTarget() {
        Permanent juggler = addReadyJuggler(player1);
        juggler.setSuspected(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        addMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune-Brand Juggler");
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice an unsuspected creature")
    void cannotSacrificeUnsuspectedCreature() {
        Permanent juggler = addReadyJuggler(player1);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(juggler.isSuspected()).isFalse();
        assertThat(fodder.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("A suspected creature has menace, cannot block, and remains suspected after cleanup")
    void suspectedDesignationPersistsAfterCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());
        castJuggler(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, creature)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Another suspected creature can pay the cost even while the Juggler is summoning sick")
    void sacrificesAnotherSuspectedCreatureAsAnImmediateCost() {
        Permanent juggler = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());
        fodder.setSuspected(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(juggler).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("An opponent's suspected creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSuspectedCreature() {
        Permanent juggler = addReadyJuggler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        target.setSuspected(true);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(juggler);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The shrink can target your own creature and expires at cleanup")
    void shrinksOwnCreatureUntilEndOfTurn() {
        Permanent juggler = addReadyJuggler(player1);
        juggler.setSuspected(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneBrandJuggler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("The ability requires the full five mana in addition to the sacrifice")
    void cannotActivateWithoutEnoughMana() {
        Permanent juggler = addReadyJuggler(player1);
        juggler.setSuspected(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneBrandJuggler());
        addCastMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(juggler);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void castJuggler(UUID targetId) {
        harness.setHand(player1, List.of(new RuneBrandJuggler()));
        addCastMana();
        harness.castCreature(player1, 0, targetId == null ? List.of() : List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyJuggler(Player player) {
        return addCreatureReady(player, new RuneBrandJuggler());
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addMana() {
        addCastMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
