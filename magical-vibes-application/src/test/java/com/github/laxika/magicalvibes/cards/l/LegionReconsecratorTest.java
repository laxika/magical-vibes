package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionReconsecrator.class, GrizzlyBears.class, SavannahLions.class, WrathOfGod.class})
class LegionReconsecratorTest extends BaseCardTest {

    @Test
    void attackExilesCreatureAndConjuresModifiedDuplicateIntoControllersGraveyard() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        Card duplicate = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.getId()).isNotEqualTo(target.getId());
        assertThat(duplicate.getColors()).contains(CardColor.GREEN, CardColor.BLACK);
        assertThat(duplicate.getSubtypes()).contains(CardSubtype.SKELETON);
        assertThat(duplicate.getPower()).isEqualTo(3);
        assertThat(duplicate.getToughness()).isEqualTo(1);
    }

    @Test
    void attackMayChooseNoCreatureCard() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void deathReturnsAnotherCreatureWithPowerOrToughnessExactlyOne() {
        addReadyLegion();
        Card validTarget = new SavannahLions();
        Card invalidTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(validTarget, invalidTarget));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validTarget.getId());
        assertThat(choice.validCardIds()).doesNotContain(invalidTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Savannah Lions");
    }

    private Permanent addReadyLegion() {
        Permanent legion = new Permanent(new LegionReconsecrator());
        legion.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(legion);
        return legion;
    }
}
