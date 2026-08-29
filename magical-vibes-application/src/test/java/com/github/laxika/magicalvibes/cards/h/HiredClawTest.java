package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiredClaw.class})
class HiredClawTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target opponent when one or more Lizards attack")
    void dealsDamageWhenLizardAttacks() {
        harness.addToBattlefield(player1, new HiredClaw());
        addLizardReady(player1);
        addLizardReady(player1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1, 2));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when no Lizard attacks")
    void doesNotTriggerWithoutLizardAttacker() {
        harness.addToBattlefield(player1, new HiredClaw());
        addCreatureReady(player1, CardSubtype.BIRD);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter ability requires an opponent to have lost life this turn")
    void counterAbilityRequiresOpponentLifeLoss() {
        addReadyHiredClaw(player1);
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    @DisplayName("Counter ability can be activated only once each turn")
    void counterAbilityOnlyOncePerTurn() {
        Permanent claw = addReadyHiredClaw(player1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(claw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        addActivationMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    private Permanent addReadyHiredClaw(Player player) {
        Permanent permanent = new Permanent(new HiredClaw());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addLizardReady(Player player) {
        addCreatureReady(player, CardSubtype.LIZARD);
    }

    private void addCreatureReady(Player player, CardSubtype subtype) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(0);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
