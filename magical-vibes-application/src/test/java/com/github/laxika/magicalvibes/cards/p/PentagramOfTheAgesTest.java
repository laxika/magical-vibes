package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PentagramOfTheAges.class, BalduvianBears.class, ZuranSpellcaster.class, Incinerate.class})
class PentagramOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        addReadyPentagram(player1);
        addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot prevention shield with no life gain")
    void choosingSourceRecordsShield() {
        addReadyPentagram(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId())
                        && s.sourceId().equals(bears.getId())
                        && !s.gainLife());
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source without gaining life")
    void preventsDamageWithoutGainingLife() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        bears.setAttacking(true);
        resolveCombat(player2);

        // 2 damage prevented, no life gained (would be 22 under Reverse Damage)
        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        Permanent chosen = addReadyBears(player2);
        Permanent other = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyPentagram(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        addReadyPentagram(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Lethal damage dealt after the source choice still ends the game")
    void lethalDamageAfterSourceChoiceEndsGame() {
        harness.setLife(player2, 2);
        addReadyPentagram(player1);
        Permanent attacker = addReadyBears(player1);
        Permanent source = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        attacker.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Prevents only the next damage event from the chosen source")
    void preventsOnlyNextDamageEventFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        Permanent spellcaster = addReadySpellcaster(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spellcaster.getId());

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        spellcaster.untap();
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can choose a spell on the stack as the damage source")
    void choosesSpellOnStackAsDamageSource() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        harness.forceActivePlayer(player2);
        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .satisfies(choice -> assertThat(choice.validIds()).contains(incinerate.getId()));
        harness.handlePermanentChosen(player1, incinerate.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyPentagram(Player player) {
        return addCreatureReady(player, new PentagramOfTheAges());
    }

    private Permanent addReadyBears(Player player) {
        return addCreatureReady(player, new BalduvianBears());
    }

    private Permanent addReadySpellcaster(Player player) {
        return addCreatureReady(player, new ZuranSpellcaster());
    }
}
