package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({KnightOfDawn.class, LightningBlast.class, LightningElemental.class})
class KnightOfDawnTest extends BaseCardTest {

    @Test
    @DisplayName("{W}{W}: gains protection from the chosen color until end of turn")
    void grantsProtectionFromChosenColor() {
        Permanent knight = addCreatureReady(player1, new KnightOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isFalse();
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without two white mana")
    void cannotActivateWithoutTwoWhiteMana() {
        Permanent knight = addCreatureReady(player1, new KnightOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Chosen-color protection stops a spell of that color from targeting it")
    void protectionStopsRedRemoval() {
        Permanent knight = addCreatureReady(player1, new KnightOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("First strike defeats a blocker before it can deal combat damage")
    void firstStrikeDealsCombatDamageBeforeRegularDamage() {
        Permanent knight = addCreatureReady(player1, new KnightOfDawn());
        Permanent blocker = addCreatureReady(player2, new LightningElemental());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(knight))));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Knight of Dawn");
        harness.assertInGraveyard(player2, "Lightning Elemental");
    }
}
