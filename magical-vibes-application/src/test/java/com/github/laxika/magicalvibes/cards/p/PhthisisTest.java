package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WringFlesh;
import com.github.laxika.magicalvibes.cards.w.WithstandDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Phthisis.class, GiantSpider.class, GrizzlyBears.class, WringFlesh.class, WithstandDeath.class})
class PhthisisTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and its controller loses life equal to power plus toughness")
    void destroysCreatureAndControllerLosesPowerPlusToughness() {
        harness.addToBattlefield(player2, new GiantSpider());
        castPhthisis();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Includes negative power when calculating the life loss")
    void includesNegativePowerInLifeLoss() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player2, 20);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Its controller loses life even when the target is indestructible")
    void controllerLosesLifeWhenTargetIsIndestructible() {
        harness.addToBattlefield(player2, new GiantSpider());
        UUID targetId = harness.getPermanentId(player2, "Giant Spider");

        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player2, 20);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Fizzles without life loss if the target is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        harness.addToBattlefield(player2, new GiantSpider());
        castPhthisis();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Suspend exiles Phthisis with five time counters and later offers a free cast")
    void suspendOffersFreeCast() {
        harness.addToBattlefield(player2, new GiantSpider());
        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        Phthisis card = new Phthisis();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Phthisis");
    }

    private void castPhthisis() {
        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
    }
}
