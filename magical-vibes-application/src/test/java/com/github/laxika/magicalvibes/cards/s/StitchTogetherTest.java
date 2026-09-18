package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.cards.f.FuneralPyre;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StitchTogether.class, AvenFogbringer.class, FuneralPyre.class})
class StitchTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature to hand without threshold")
    void returnsTargetCreatureToHandWithoutThreshold() {
        Card creature = new AvenFogbringer();
        harness.setGraveyard(player1, List.of(creature,
                new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre()));
        castStitchTogether(creature);

        harness.assertInHand(player1, "Aven Fogbringer");
        harness.assertNotOnBattlefield(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("Returns the target creature to the battlefield with threshold")
    void returnsTargetCreatureToBattlefieldWithThreshold() {
        Card creature = new AvenFogbringer();
        harness.setGraveyard(player1, List.of(creature,
                new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(),
                new FuneralPyre()));
        castStitchTogether(creature);

        harness.assertOnBattlefield(player1, "Aven Fogbringer");
        harness.assertNotInHand(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("Checks threshold when the spell resolves")
    void checksThresholdWhenSpellResolves() {
        Card creature = new AvenFogbringer();
        harness.setGraveyard(player1, List.of(creature,
                new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(),
                new FuneralPyre()));
        harness.setHand(player1, List.of(new StitchTogether()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of(creature,
                new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre(), new FuneralPyre()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aven Fogbringer");
        harness.assertNotOnBattlefield(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("Does nothing if the target leaves the graveyard before resolution")
    void doesNothingIfTargetLeavesGraveyardBeforeResolution() {
        Card creature = new AvenFogbringer();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new StitchTogether()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertNotInHand(player1, "Aven Fogbringer");
        harness.assertNotOnBattlefield(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new FuneralPyre();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new StitchTogether()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new AvenFogbringer();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new StitchTogether()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    private void castStitchTogether(Card target) {
        harness.setHand(player1, List.of(new StitchTogether()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
