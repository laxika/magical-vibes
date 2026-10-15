package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChalkOutline.class, Disentomb.class, GrizzlyBears.class, Recollect.class, Reminisce.class, Shock.class})
class ChalkOutlineTest extends BaseCardTest {

    @Test
    void createsDetectiveAndClueWhenCreatureCardLeavesYourGraveyard() {
        addReadyOutline();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenNoncreatureCardLeavesYourGraveyard() {
        addReadyOutline();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, shock.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void createsOnlyOneDetectiveAndClueWhenSeveralCreatureCardsLeaveTogether() {
        addReadyOutline();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void createsWhiteAndBlueTwoTwoDetectiveCreatureToken() {
        addReadyOutline();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        resolveAllTriggers();

        var detective = findPermanent(player1, "Detective").getCard();
        assertThat(detective.isToken()).isTrue();
        assertThat(detective.hasType(CardType.CREATURE)).isTrue();
        assertThat(detective.getPower()).isEqualTo(2);
        assertThat(detective.getToughness()).isEqualTo(2);
        assertThat(detective.getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getSubtypes()).containsExactly(CardSubtype.DETECTIVE);
    }

    @Test
    void doesNotTriggerWhenCreatureCardsLeaveOpponentsGraveyard() {
        addReadyOutline();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Detective")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void triggersForEachSeparateCreatureCardRemovalInTheSameTurn() {
        addReadyOutline();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        harness.castSorcery(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).hasSize(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDrawACard() {
        addReadyOutline();
        GrizzlyBears bears = new GrizzlyBears();
        Shock drawnCard = new Shock();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, bears.getId());
        resolveAllTriggers();

        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void addReadyOutline() {
        harness.addToBattlefield(player1, new ChalkOutline());
    }
}
