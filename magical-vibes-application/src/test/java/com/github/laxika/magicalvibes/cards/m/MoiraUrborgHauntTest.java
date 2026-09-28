package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoiraUrborgHaunt.class, GrizzlyBears.class, DoomBlade.class})
class MoiraUrborgHauntTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature that died from the battlefield this turn")
    void combatDamageReturnsEligibleCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cards not put into the graveyard from the battlefield this turn are not eligible")
    void ignoresCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
