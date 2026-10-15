package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawItComing.class, GrizzledOutrider.class})
class SawItComingTest extends BaseCardTest {

    @Test
    void countersTargetSpell() {
        GrizzledOutrider creature = new GrizzledOutrider();

        harness.setHand(player2, List.of(new SawItComing()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, creature, "{4}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzled Outrider");
        harness.assertInGraveyard(player2, "Saw It Coming");
    }

    @Test
    void foretellsAndCastsOnALaterTurn() {
        SawItComing spell = new SawItComing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GrizzledOutrider creature = new GrizzledOutrider();
        harness.castFromHand(player1, creature, "{4}{G}");
        harness.castFromExile(player1, spell.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saw It Coming");
        harness.assertInGraveyard(player1, "Grizzled Outrider");
    }

    @Test
    void castsForForetellCostDuringOpponentsLaterTurn() {
        SawItComing spell = new SawItComing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        GrizzledOutrider creature = new GrizzledOutrider();
        harness.castFromHand(player2, creature, "{4}{G}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzled Outrider");
        harness.assertInGraveyard(player1, "Saw It Coming");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        SawItComing spell = new SawItComing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        GrizzledOutrider creature = new GrizzledOutrider();
        harness.castFromHand(player1, creature, "{4}{G}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void countersAnInstantSpell() {
        GrizzledOutrider creature = new GrizzledOutrider();
        harness.castFromHand(player1, creature, "{4}{G}");
        SawItComing opposingCounter = new SawItComing();
        harness.setHand(player2, List.of(opposingCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.setHand(player1, List.of(new SawItComing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, opposingCounter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saw It Coming");
        harness.assertInGraveyard(player2, "Saw It Coming");
        harness.assertOnBattlefield(player1, "Grizzled Outrider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellWithoutTwoMana() {
        SawItComing spell = new SawItComing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        SawItComing spell = new SawItComing();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }
}
