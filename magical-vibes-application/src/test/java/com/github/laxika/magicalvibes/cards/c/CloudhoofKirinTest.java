package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.i.IdeasUnbound;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudhoofKirin.class, ArabaMothrider.class, GhostLitRedeemer.class, IdeasUnbound.class,
        SpiritualVisit.class})
class CloudhoofKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell may mill a player by that spell's mana value")
    void arcaneSpellMillsByManaValue() {
        addCloudhoofKirin();
        harness.setLibrary(player2, List.of(new ArabaMothrider(), new ArabaMothrider(),
                new ArabaMothrider(), new ArabaMothrider()));

        harness.castFromHand(player1, new IdeasUnbound(), "{U}{U}");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a Spirit spell may mill one card")
    void spiritSpellMillsOneCard() {
        addCloudhoofKirin();
        harness.setLibrary(player2, List.of(new ArabaMothrider(), new ArabaMothrider()));

        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the trigger mills nothing")
    void decliningDoesNothing() {
        addCloudhoofKirin();
        harness.setLibrary(player2, List.of(new ArabaMothrider(), new ArabaMothrider()));

        harness.castFromHand(player1, new IdeasUnbound(), "{U}{U}");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addCloudhoofKirin();
        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent casting a Spirit or Arcane spell does not trigger it")
    void opponentSpellDoesNotTrigger() {
        addCloudhoofKirin();

        harness.castFromHand(player2, new SpiritualVisit(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The mill trigger offers players rather than permanents as targets")
    void millTriggerOffersOnlyPlayerTargets() {
        addCloudhoofKirin();
        var permanent = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.setLibrary(player2, List.of(new ArabaMothrider(), new ArabaMothrider()));

        harness.castFromHand(player1, new IdeasUnbound(), "{U}{U}");
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPermanentIds()).isEmpty();
        assertThat(targetChoice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(targetChoice.validIds()).doesNotContain(permanent.getId());
    }

    private void addCloudhoofKirin() {
        harness.addToBattlefield(player1, new CloudhoofKirin());
    }
}
