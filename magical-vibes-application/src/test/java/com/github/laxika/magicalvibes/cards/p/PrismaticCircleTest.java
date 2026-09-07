package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({PrismaticCircle.class, ViashinoWarrior.class, GiantMantis.class})
class PrismaticCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color as it enters sets chosenColor on the enchantment")
    void choosingColorOnEnter() {
        harness.setHand(player1, List.of(new PrismaticCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Prismatic Circle").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Only sources of the chosen color may be chosen for the prevention shield")
    void onlyChosenColorSourcesAreValid() {
        addCircle(player1, CardColor.RED);
        Permanent viashino = addCreatureReady(player2, new ViashinoWarrior());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(viashino.getId()).doesNotContain(mantis.getId());

        harness.handlePermanentChosen(player1, viashino.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(viashino.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source and consumes the shield")
    void preventsNextCombatDamage() {
        harness.setLife(player1, 20);
        addCircle(player1, CardColor.RED);
        Permanent viashino = addCreatureReady(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, viashino.getId());

        viashino.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from an unchosen source color is dealt and does not consume the shield")
    void doesNotPreventDamageFromUnchosenSource() {
        harness.setLife(player1, 20);
        addCircle(player1, CardColor.RED);
        Permanent viashino = addCreatureReady(player2, new ViashinoWarrior());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, viashino.getId());

        mantis.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(viashino.getId()));
    }

    @Test
    @CardUsed(Incinerate.class)
    @DisplayName("A chosen source spell has its next damage to you prevented")
    void preventsDamageFromChosenSpellOnStack() {
        harness.setLife(player1, 20);
        addCircle(player1, CardColor.RED);

        Incinerate incinerate = new Incinerate();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(incinerate.getId());
        harness.handlePermanentChosen(player1, incinerate.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Incinerate");
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("No source choice is offered when no permanent matches the chosen color")
    void noMatchingColorSource() {
        addCircle(player1, CardColor.BLUE);
        addCreatureReady(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps the circle")
    void paysCumulativeUpkeep() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new PrismaticCircle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(circle.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(circle);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the circle")
    void declineSacrifices() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new PrismaticCircle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(circle);
        harness.assertInGraveyard(player1, "Prismatic Circle");
    }

    private Permanent addCircle(Player player, CardColor chosen) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PrismaticCircle());
        perm.setChosenColor(chosen);
        return perm;
    }
}
