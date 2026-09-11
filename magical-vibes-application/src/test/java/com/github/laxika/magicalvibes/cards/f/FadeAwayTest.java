package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FadeAway.class, RagingGoblin.class, Spellbook.class, CityOfTraitors.class})
class FadeAwayTest extends BaseCardTest {

    @Test
    @DisplayName("A player can pay for some creatures and sacrifice other permanents for the rest")
    void paysForSomeCreaturesAndSacrificesOtherPermanent() {
        Permanent keptCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(keptCreature.getId()));

        harness.handleMultiplePermanentsChosen(player2, List.of(spellbook.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Raging Goblin")).hasSize(2);
        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Pays once for each creature when enough mana is available")
    void paysForEachCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Raging Goblin")).hasSize(2);
        harness.assertNotInGraveyard(player2, "Spellbook");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrifices are deferred until every player has made their choice")
    void sacrificesAreDeferredUntilAllPlayersChoose() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, ownArtifact);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentArtifact.getId()));

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
    }

    @Test
    @DisplayName("A player can activate a mana ability while choosing creatures to pay for")
    void canActivateManaAbilityWhileChoosingPayments() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent cityOfTraitors = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(2);
        gs.activateAbility(gd, player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(cityOfTraitors),
                null, null, null, null);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Raging Goblin")).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }
}
