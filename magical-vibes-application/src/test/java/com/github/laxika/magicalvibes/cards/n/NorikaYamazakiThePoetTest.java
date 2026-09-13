package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorikaYamazakiThePoet.class, MothriderSamurai.class, GloriousAnthem.class, Shock.class,
        GrizzlyBears.class})
class NorikaYamazakiThePoetTest extends BaseCardTest {

    @Test
    @DisplayName("A lone Samurai may cast an enchantment from the graveyard this turn")
    void loneSamuraiMayCastEnchantmentFromGraveyard() {
        addCreatureReady(player1, new NorikaYamazakiThePoet());
        addCreatureReady(player1, new MothriderSamurai());
        Card anthem = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(anthem, new Shock()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, anthem.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("The trigger does not fire for a non-Samurai, non-Warrior attacker")
    void triggerDoesNotFireForOtherCreature() {
        addCreatureReady(player1, new NorikaYamazakiThePoet());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GloriousAnthem()));

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire when a Samurai or Warrior attacks with another creature")
    void triggerDoesNotFireWhenAttackingWithAnotherCreature() {
        addCreatureReady(player1, new NorikaYamazakiThePoet());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GloriousAnthem()));

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the trigger does not grant permission to cast from the graveyard")
    void decliningTriggerDoesNotGrantPermission() {
        addCreatureReady(player1, new NorikaYamazakiThePoet());
        addCreatureReady(player1, new MothriderSamurai());
        Card anthem = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(anthem));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }
}
