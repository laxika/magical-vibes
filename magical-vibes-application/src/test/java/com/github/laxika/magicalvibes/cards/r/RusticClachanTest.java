package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RusticClachanTest extends BaseCardTest {

    // ===== Enters tapped / reveal choice =====

    @Test
    @DisplayName("Enters tapped when you have no Kithkin card in hand")
    void entersTappedWithoutKithkin() {
        harness.setHand(player1, List.of(new RusticClachan(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Kithkin lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new RusticClachan(), new GoldmeadowStalwart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Kithkin in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new RusticClachan(), new GoldmeadowStalwart()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent land = new Permanent(new RusticClachan());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    // ===== Reinforce =====

    @Test
    @DisplayName("Reinforce puts a +1/+1 counter on target creature and discards the source")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Rustic Clachan");
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature; the card stays in hand")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new RusticClachan()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rustic Clachan");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent findLand(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rustic Clachan"))
                .findFirst().orElseThrow();
    }
}
