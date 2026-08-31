package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CarrierPigeons;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildAesthir.class, ElvishRanger.class, CarrierPigeons.class})
class WildAesthirTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +2/+0 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent aesthir = addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aesthir)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability does not tap Wild Aesthir")
    void abilityDoesNotTapSource() {
        Permanent aesthir = addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aesthir.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent aesthir = addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability requires two white mana")
    void abilityRequiresTwoWhiteMana() {
        addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new WildAesthir());
        addCreatureReady(player2, new ElvishRanger());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike destroys a one-toughness blocker before it deals damage")
    void firstStrikeDealsCombatDamageBeforeBlocker() {
        Permanent aesthir = addCreatureReady(player1, new WildAesthir());
        Permanent carrier = addCreatureReady(player2, new CarrierPigeons());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aesthir);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(carrier);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The once-per-turn limit resets on the next turn")
    void oncePerTurnLimitResetsOnNextTurn() {
        Permanent aesthir = addCreatureReady(player1, new WildAesthir());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player2);

        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aesthir)).isEqualTo(3);
    }
}
