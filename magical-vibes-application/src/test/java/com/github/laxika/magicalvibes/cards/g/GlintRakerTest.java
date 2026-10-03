package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintRaker.class, WornPowerstone.class, Memnite.class, Shock.class})
class GlintRakerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 from the greatest mana value among artifacts you control")
    void getsPowerFromGreatestArtifactManaValue() {
        Permanent raker = addCreatureReady(player1, new GlintRaker());
        harness.addToBattlefield(player1, new Memnite());
        assertThat(gqs.getEffectivePower(gd, raker)).isEqualTo(1);

        harness.addToBattlefield(player1, new WornPowerstone());

        assertThat(gqs.getEffectivePower(gd, raker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage may reveal that many cards and put one artifact into hand")
    void revealsAndKeepsOneArtifact() {
        WornPowerstone powerstone = new WornPowerstone();
        Memnite memnite = new Memnite();
        Shock shock = new Shock();
        Shock secondShock = new Shock();
        harness.setLibrary(player1, List.of(powerstone, memnite, shock, secondShock));

        addReadyRakerWithPowerstone();
        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(powerstone);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(memnite, shock, secondShock);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the library unchanged")
    void declinesReveal() {
        List<Card> library = List.of(new WornPowerstone(), new Memnite(), new Shock(), new Shock());
        harness.setLibrary(player1, library);
        addReadyRakerWithPowerstone();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addReadyRakerWithPowerstone() {
        harness.addToBattlefield(player1, new WornPowerstone());
        addCreatureReady(player1, new GlintRaker());
    }
}
