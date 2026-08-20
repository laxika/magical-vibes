package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShikoParagonOfTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only a nonland card with mana value 3 or less from your graveyard")
    void etbFiltersGraveyardTargets() {
        CounselOfTheSoratami valid = new CounselOfTheSoratami();
        Forest land = new Forest();
        AirElemental expensive = new AirElemental();
        CounselOfTheSoratami opponentCard = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(valid, land, expensive));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new ShikoParagonOfTheWay()));
        addShikoMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());
    }

    @Test
    @DisplayName("Exiles the target and may cast a copy, with permanent copies becoming tokens")
    void exilesAndCastsCopy() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ShikoParagonOfTheWay()));
        addShikoMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isToken());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    private void addShikoMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
