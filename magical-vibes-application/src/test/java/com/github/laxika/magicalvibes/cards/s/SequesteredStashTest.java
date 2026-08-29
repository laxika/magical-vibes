package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SequesteredStashTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent stash = addReadyStash();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stash.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void sacrificesMillsFiveAndMayPutsAnArtifactOnTop() {
        Permanent stash = addReadyStash();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Ornithopter())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stash);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Ornithopter");
        harness.assertInGraveyard(player1, "Sequestered Stash");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void decliningMayChoiceLeavesArtifactInGraveyard() {
        addReadyStash();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Ornithopter())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void doesNotOfferNonArtifactCards() {
        addReadyStash();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyStash() {
        Permanent stash = new Permanent(new SequesteredStash());
        stash.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stash);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return stash;
    }
}
