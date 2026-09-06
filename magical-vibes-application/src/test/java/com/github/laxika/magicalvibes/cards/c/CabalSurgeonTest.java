package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalSurgeon.class, GrizzlyBears.class, LightningBolt.class})
class CabalSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two graveyard cards and returns a target creature card to hand")
    void exilesTwoCardsAndReturnsTargetCreature() {
        Permanent surgeon = addReadySurgeon();
        Card creature = new GrizzlyBears();
        Card other = new LightningBolt();
        Card third = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(other, third, creature)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(other.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(other.getId(), third.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent surgeon = addReadySurgeon();
        Card target = new LightningBolt();
        Card costCard = new LightningBolt();
        Card otherCostCard = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target, costCard, otherCostCard)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate without two cards in the graveyard to exile")
    void cannotActivateWithoutTwoCardsToExile() {
        Permanent surgeon = addReadySurgeon();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySurgeon() {
        Permanent surgeon = new Permanent(new CabalSurgeon());
        surgeon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(surgeon);
        return surgeon;
    }

    private int index(Permanent surgeon) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(surgeon);
    }
}
