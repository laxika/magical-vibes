package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianShaman;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShikoParagonOfTheWay.class, AirElemental.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class, ViridianShaman.class})
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
        harness.castFromHand(player1, new ShikoParagonOfTheWay(), "{2}{U}{R}{W}");
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
        harness.castFromHand(player1, new ShikoParagonOfTheWay(), "{2}{U}{R}{W}");
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

    @Test
    @DisplayName("Declining the copy still exiles the original card")
    void decliningCopyStillExilesOriginal() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new ShikoParagonOfTheWay(), "{2}{U}{R}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copied sorcery resolves without mana and the original stays exiled")
    void copiedSorceryDrawsCards() {
        CounselOfTheSoratami target = new CounselOfTheSoratami();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new ShikoParagonOfTheWay(), "{2}{U}{R}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player1, "Counsel of the Soratami");
    }

    @Test
    @DisplayName("A creature copy can be cast when its ETB ability has no legal target")
    void creatureWithTargetedEtbCanBeCastWithoutEtbTargets() {
        ViridianShaman target = new ViridianShaman();
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new ShikoParagonOfTheWay(), "{2}{U}{R}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Viridian Shaman")
                        && permanent.getCard().isToken());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }
}
