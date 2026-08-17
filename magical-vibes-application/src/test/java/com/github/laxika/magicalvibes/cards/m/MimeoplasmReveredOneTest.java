package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MimeoplasmReveredOneTest extends BaseCardTest {

    private void castMimeoplasm(int xValue) {
        harness.setHand(player1, new ArrayList<>(List.of(new MimeoplasmReveredOne())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private Permanent mimeoplasm() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mimeoplasm, Revered One"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Exiles up to X creature cards and gets three counters for each chosen card")
    void exilesUpToXCreaturesWithCounters() {
        GrizzlyBears bears = new GrizzlyBears();
        GiantSpider spider = new GiantSpider();
        HillGiant giant = new HillGiant();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, spider, giant, pacifism)));

        castMimeoplasm(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), spider.getId(), giant.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), spider.getId()));

        Permanent mimeoplasm = mimeoplasm();
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, mimeoplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mimeoplasm)).isEqualTo(6);
        assertThat(gd.getCardsExiledByPermanent(mimeoplasm.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(bears.getId(), spider.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(giant.getId(), pacifism.getId());
    }

    @Test
    @DisplayName("Becomes a 0/0 copy of a creature card exiled with it and keeps this ability")
    void copiesExiledCreatureAndKeepsAbility() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        castMimeoplasm(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent mimeoplasm = mimeoplasm();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(mimeoplasm.getCard().getPower()).isZero();
        assertThat(mimeoplasm.getCard().getToughness()).isZero();
        assertThat(mimeoplasm.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mimeoplasm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimeoplasm)).isEqualTo(3);
    }
}
