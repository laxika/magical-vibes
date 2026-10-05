package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Meteorite.class, RuneclawBear.class, ChandraPyromaster.class})
class MeteoriteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to a target creature, killing a 2/2")
    void etbDealsTwoDamageToCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castArtifact(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteorite");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("ETB deals 2 damage to a target player")
    void etbDealsTwoDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tap ability prompts for a color and adds one mana of it")
    void manaAbilityAddsChosenColor() {
        Permanent meteorite = harness.addToBattlefieldAndReturn(player1, new Meteorite());
        meteorite.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(meteorite.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Meteorite can target its controller and produce mana before its ETB trigger resolves")
    void manaAbilityWorksWhileEtbTriggerIsPending() {
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Meteorite");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB damage removes two loyalty counters from a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0, chandra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Chandra, Pyromaster");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A newly controlled Meteorite can immediately produce each color without using the stack")
    void newlyControlledArtifactProducesAnyColor(ManaColor color) {
        Permanent meteorite = harness.addToBattlefieldAndReturn(player1, new Meteorite());
        meteorite.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(meteorite.isTapped()).isTrue();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }
}
