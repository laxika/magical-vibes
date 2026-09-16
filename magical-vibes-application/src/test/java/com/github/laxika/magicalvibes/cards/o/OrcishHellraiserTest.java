package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishHellraiser.class, Murder.class, GrizzlyBears.class})
class OrcishHellraiserTest extends BaseCardTest {

    @Test
    @DisplayName("When Orcish Hellraiser dies, it deals 2 damage to the chosen player")
    void deathTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new OrcishHellraiser());
        killHellraiser();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The death trigger can target a planeswalker but not a creature")
    void deathTriggerTargetsPlaneswalker() {
        harness.addToBattlefield(player1, new OrcishHellraiser());
        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID planeswalkerId = planeswalker.getId();

        killHellraiser();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(planeswalkerId)
                .doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Declining echo sacrifices Orcish Hellraiser")
    void decliningEchoSacrificesHellraiser() {
        castHellraiser();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Orcish Hellraiser");
        harness.assertInGraveyard(player1, "Orcish Hellraiser");
    }

    @Test
    @DisplayName("Paying echo keeps Orcish Hellraiser on the battlefield")
    void payingEchoKeepsHellraiser() {
        castHellraiser();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Orcish Hellraiser");
    }

    private void castHellraiser() {
        harness.setHand(player1, List.of(new OrcishHellraiser()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killHellraiser() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        UUID hellraiserId = harness.getPermanentId(player1, "Orcish Hellraiser");
        harness.castInstant(player2, 0, hellraiserId);
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        com.github.laxika.magicalvibes.model.Card card = new com.github.laxika.magicalvibes.model.Card();
        card.setName("Test Planeswalker");
        card.setType(com.github.laxika.magicalvibes.model.CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
