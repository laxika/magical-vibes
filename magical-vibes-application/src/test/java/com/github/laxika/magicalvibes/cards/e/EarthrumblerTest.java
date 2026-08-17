package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EarthrumblerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling an artifact or creature card from the graveyard animates Earthrumbler")
    void exilingArtifactOrCreatureAnimatesEarthrumbler() {
        Permanent earthrumbler = addReadyEarthrumbler();
        harness.setGraveyard(player1, List.of(new Shock(), new Spellbook(), new GrizzlyBears()));

        harness.activateAbility(player1, battlefieldIndex(earthrumbler), 0, null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
        assertThat(gqs.isCreature(gd, earthrumbler)).isTrue();
    }

    @Test
    @DisplayName("Cannot exile a card that is neither an artifact nor a creature")
    void cannotExileInvalidGraveyardCard() {
        Permanent earthrumbler = addReadyEarthrumbler();
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(earthrumbler), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates Earthrumbler and taps the crew")
    void crewAnimatesEarthrumbler() {
        Permanent earthrumbler = addReadyEarthrumbler();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        memnite.setSummoningSick(false);

        harness.activateAbility(player1, battlefieldIndex(earthrumbler), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, earthrumbler)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }

    private Permanent addReadyEarthrumbler() {
        Permanent earthrumbler = harness.addToBattlefieldAndReturn(player1, new Earthrumbler());
        earthrumbler.setSummoningSick(false);
        return earthrumbler;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
