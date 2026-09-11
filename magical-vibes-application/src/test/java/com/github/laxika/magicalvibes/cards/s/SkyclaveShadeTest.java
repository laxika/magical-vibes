package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclaveShade.class, Forest.class, GrizzlyBears.class})
class SkyclaveShadeTest extends BaseCardTest {

    @Test
    @DisplayName("A kicked Skyclave Shade enters with two +1/+1 counters")
    void kickedEntersWithTwoCounters() {
        SkyclaveShade shade = new SkyclaveShade();
        harness.setHand(player1, List.of(shade));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = battlefieldPermanent(player1, shade);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Skyclave Shade cannot block")
    void cannotBlock() {
        Permanent shade = addCreatureReady(player2, new SkyclaveShade());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(shade.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A landfall trigger grants a normal-cost cast from the graveyard")
    void landfallGrantsCastFromGraveyard() {
        SkyclaveShade shade = new SkyclaveShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(battlefieldPermanent(player1, shade)).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shade.getId()));
    }

    @Test
    @DisplayName("Declining the landfall trigger keeps Skyclave Shade in the graveyard")
    void decliningLandfallKeepsItInGraveyard() {
        SkyclaveShade shade = new SkyclaveShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(shade.getId()));
    }

    @Test
    @DisplayName("An opponent's land does not trigger Skyclave Shade")
    void opponentLandDoesNotTrigger() {
        SkyclaveShade shade = new SkyclaveShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent battlefieldPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
