package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtlaPalaniNestTender.class, CarrionFeeder.class, Forest.class, GrizzlyBears.class})
class AtlaPalaniNestTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 0/1 green Egg creature token with defender")
    void createsEggToken() {
        Permanent atla = addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent egg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(atla.isTapped()).isTrue();
        assertThat(egg.getCard().getPower()).isZero();
        assertThat(egg.getCard().getToughness()).isEqualTo(1);
        assertThat(egg.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(egg.getCard().getSubtypes()).containsExactly(CardSubtype.EGG);
        assertThat(gqs.hasKeyword(gd, egg, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Puts a creature from the library onto the battlefield when an Egg dies")
    void eggDeathRevealsCreatureToBattlefield() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Card forestInLibrary = new Forest();
        Card creatureInLibrary = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forestInLibrary, creatureInLibrary));
        createEgg();

        Permanent egg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.EGG))
                .findFirst()
                .orElseThrow();
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, egg.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .anyMatch(card -> card.getId().equals(creatureInLibrary.getId()))).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forestInLibrary);
    }

    @Test
    @DisplayName("Does not trigger when a non-Egg creature dies")
    void nonEggDeathDoesNotTrigger() {
        addCreatureReady(player1, new AtlaPalaniNestTender());
        harness.addToBattlefield(player1, new CarrionFeeder());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card creatureInLibrary = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creatureInLibrary));

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureInLibrary);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .filter(card -> card.getId().equals(creatureInLibrary.getId())))
                .isEmpty();
    }

    private void createEgg() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

}
