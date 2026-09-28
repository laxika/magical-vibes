package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerrardWeatherlightHero.class, GrizzlyBears.class, ObsidianBattleAxe.class,
        Naturalize.class, WrathOfGod.class})
class GerrardWeatherlightHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger exiles Gerrard and returns this turn's artifacts and creatures")
    void deathTriggerExilesGerrardAndReturnsArtifactsAndCreatures() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Card bears = new GrizzlyBears();
        Card axe = new ObsidianBattleAxe();
        Permanent gerrard = harness.addToBattlefieldAndReturn(player1, new GerrardWeatherlightHero());
        harness.addToBattlefield(player1, bears);
        Permanent battleAxe = harness.addToBattlefieldAndReturn(player1, axe);
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, battleAxe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Obsidian Battle-Axe");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gerrard.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(gerrard.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(alreadyInGraveyard)
                .noneMatch(card -> card.getId().equals(bears.getId()) || card.getId().equals(axe.getId()));
    }

    @Test
    @DisplayName("Death trigger does not return cards already in the graveyard")
    void deathTriggerOnlyReturnsCardsPutThereThisTurn() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Permanent gerrard = harness.addToBattlefieldAndReturn(player1, new GerrardWeatherlightHero());
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gerrard.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(alreadyInGraveyard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
