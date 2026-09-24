package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfFellRites.class, GrizzlyBears.class, HolyDay.class})
class PriestOfFellRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps, pays 3 life, sacrifices itself, and returns a creature from the graveyard")
    void returnsCreatureFromGraveyardToBattlefield() {
        addCreatureReady(player1, new PriestOfFellRites());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertNotOnBattlefield(player1, "Priest of Fell Rites");
        harness.assertInGraveyard(player1, "Priest of Fell Rites");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only targets a creature card in the controller's graveyard")
    void rejectsIllegalGraveyardTargets() {
        addCreatureReady(player1, new PriestOfFellRites());
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reanimation ability can only be activated at sorcery speed")
    void reanimationAbilityOnlyAtSorcerySpeed() {
        addCreatureReady(player1, new PriestOfFellRites());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth returns Priest of Fell Rites with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        PriestOfFellRites card = new PriestOfFellRites();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Priest of Fell Rites");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Priest of Fell Rites");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Priest of Fell Rites"));
    }
}
