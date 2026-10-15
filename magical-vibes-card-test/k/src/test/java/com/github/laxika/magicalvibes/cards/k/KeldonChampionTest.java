package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.r.Rescue;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonChampion.class, ChandraNalaar.class, Rescue.class})
class KeldonChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals 3 damage to the targeted player")
    void etbDealsDamageToPlayer() {
        castAndResolveChampion(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Keldon Champion");
    }

    @Test
    @DisplayName("ETB damage can target its controller")
    void etbDealsDamageToController() {
        castAndResolveChampion(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("ETB damage can target a planeswalker")
    void etbDealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castAndResolveChampion(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB damage cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KeldonChampion());
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
        resolveAllTriggers();
    }

    @Test
    @CardUsed({ChandraNalaar.class, Rescue.class})
    @DisplayName("Echo still triggers when the ETB damage target leaves before resolution")
    void echoStillTriggersWhenDamageTargetBecomesIllegal() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        prepareChampion();
        harness.castCreature(player1, 0, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Rescue()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, planeswalker.getId());
        resolveAllTriggers();
        harness.assertInHand(player2, "Chandra Nalaar");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Keldon Champion");
    }

    @Test
    @DisplayName("Haste allows attacking the turn Keldon Champion enters")
    void canAttackOnTheTurnItEnters() {
        castAndResolveChampion(player2.getId());

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        harness.assertLife(player2, 14);
    }
}
