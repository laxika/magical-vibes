package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothsDayPlanner.class, DarkRitual.class, GrizzlyBears.class, Shock.class})
class YawgmothsDayPlannerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mana ability pays two life and adds graveyard-only black mana")
    void activatesManaAbility() {
        harness.addToBattlefield(player1, new YawgmothsDayPlanner());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.BLACK))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("The graveyard-only mana casts a spell from the graveyard")
    void castsSpellFromGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsDayPlanner());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new DarkRitual()));

        harness.activateAbility(player1, 0, null, null);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dark Ritual"));
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.BLACK))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard-only mana cannot cast a spell from hand")
    void cannotUseGraveyardOnlyManaFromHand() {
        harness.addToBattlefield(player1, new YawgmothsDayPlanner());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DarkRitual()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cards owned by the controller are exiled instead of entering their graveyard")
    void exilesOwnCardsInsteadOfGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsDayPlanner());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Shock"));
    }
}
