package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianaTheNecromancer.class, GreenwoodSentinel.class})
class LilianaTheNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes a target player lose 2 life")
    void plusOneMakesTargetPlayerLoseLife() {
        Permanent liliana = addReadyLiliana(player1, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("-1 returns a target creature card from the graveyard to hand")
    void minusOneReturnsTargetCreatureToHand() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Card creature = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("-7 destroys up to two creatures and returns up to two creatures from all graveyards")
    void minusSevenDestroysAndReturnsCreatures() {
        Permanent liliana = addReadyLiliana(player1, 7);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        Card ownCreature = new GreenwoodSentinel();
        Card opponentCreature = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .toList();
        harness.activateAbilityWithMultiTargets(player1, 0, 2, targets);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        chooseGraveyardCard(ownCreature.getId());
        harness.handleMayAbilityChosen(player1, true);
        chooseGraveyardCard(opponentCreature.getId());

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        harness.assertNotOnBattlefield(player1, "Liliana, the Necromancer");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Greenwood Sentinel")))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ownCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("+1 can target its controller")
    void plusOneCanTargetController() {
        addReadyLiliana(player1, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("-7 can choose no targets and return no creatures")
    void minusSevenCanDeclineBothReturns() {
        addReadyLiliana(player1, 8);
        Card creature = new GreenwoodSentinel();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("-7 can return the creature it just destroyed")
    void minusSevenReturnsDestroyedCreature() {
        addReadyLiliana(player1, 8);
        Card creature = new GreenwoodSentinel();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        chooseGraveyardCard(creature.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("-7 chooses both returned creatures before either enters the battlefield")
    void minusSevenReturnsCreaturesTogether() {
        addReadyLiliana(player1, 8);
        Card first = new GreenwoodSentinel();
        Card second = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        chooseGraveyardCard(first.getId());

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");

        harness.handleMayAbilityChosen(player1, true);
        chooseGraveyardCard(second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Greenwood Sentinel")))
                .hasSize(2);
    }

    private void chooseGraveyardCard(UUID cardId) {
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int index = choice.cardPool().stream()
                .map(Card::getId)
                .toList()
                .indexOf(cardId);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleGraveyardCardChosen(player1, index);
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LilianaTheNecromancer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
