package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeldonChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals 3 damage to the targeted player")
    void etbDealsDamageToPlayer() {
        castAndResolveChampion(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Keldon Champion");
    }

    @Test
    @DisplayName("ETB damage can target a planeswalker")
    void etbDealsDamageToPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 5);
        castAndResolveChampion(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB damage cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        prepareChampion();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining echo sacrifices Keldon Champion at its next upkeep")
    void decliningEchoSacrificesChampion() {
        castAndResolveChampion(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Keldon Champion");
        harness.assertInGraveyard(player1, "Keldon Champion");
    }

    @Test
    @DisplayName("Paying echo keeps the creature and echo does not trigger again")
    void payingEchoKeepsChampionAndIsOneShot() {
        castAndResolveChampion(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Keldon Champion");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Keldon Champion");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentUpkeep() {
        castAndResolveChampion(player2.getId());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Keldon Champion");
    }

    private void prepareChampion() {
        harness.setHand(player1, List.of(new KeldonChampion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void castAndResolveChampion(java.util.UUID targetId) {
        prepareChampion();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
