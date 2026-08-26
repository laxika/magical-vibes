package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuneBrandJuggler.class, GrizzlyBears.class})
class RuneBrandJugglerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB suspects up to one target creature you control")
    void entersAndSuspectsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(7);
        targetCard.setToughness(7);
        harness.addToBattlefield(player2, targetCard);
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        addMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, juggler.getId());
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
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(juggler.isSuspected()).isFalse();
        assertThat(fodder.isSuspected()).isFalse();
    }

    private void castJuggler(UUID targetId) {
        harness.setHand(player1, List.of(new RuneBrandJuggler()));
        addCastMana();
        harness.castCreature(player1, 0, targetId == null ? List.of() : List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyJuggler(Player player) {
        Permanent permanent = new Permanent(new RuneBrandJuggler());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
