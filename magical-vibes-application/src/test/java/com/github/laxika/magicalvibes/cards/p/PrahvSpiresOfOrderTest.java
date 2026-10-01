package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.r.RakdosIckspitter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrahvSpiresOfOrder.class, MistralCharger.class, RakdosIckspitter.class, CacklingFlames.class})
class PrahvSpiresOfOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage prevention ability requires its mana cost and tap")
    void preventionAbilityRequiresManaAndTap() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        addPreventionMana();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the prevention ability prompts for a source")
    void promptsForSource() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(source.getId());
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(source.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(source.getId());
    }

    @Test
    @DisplayName("Prevents all combat damage from only the chosen source")
    void preventsAllCombatDamageOnlyFromChosenSource() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent chosenSource = addCreatureReady(player2, new MistralCharger());
        Permanent otherSource = addCreatureReady(player2, new MistralCharger());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.permanentsPreventedFromDealingDamage).contains(chosenSource.getId());
    }

    @Test
    @DisplayName("Prevents noncombat damage from the chosen source")
    void preventsNoncombatDamageFromChosenSource() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent source = addCreatureReady(player2, new RakdosIckspitter());
        Permanent target = addCreatureReady(player1, new MistralCharger());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can choose a damaging spell on the stack as the source")
    void preventsDamageFromChosenSpellOnStack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());

        CacklingFlames flames = new CacklingFlames();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(flames));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, player1.getId());

        addPreventionMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(flames.getId());

        harness.handlePermanentChosen(player1, flames.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Cackling Flames");
    }

    private void addPreventionMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
