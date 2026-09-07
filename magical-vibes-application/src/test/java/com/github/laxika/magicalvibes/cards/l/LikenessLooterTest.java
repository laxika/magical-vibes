package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LikenessLooter.class, GrizzlyBears.class, HillGiant.class, Island.class})
class LikenessLooterTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws a card, then prompts for a discard")
    void tapAbilityDrawsThenDiscards() {
        addCreatureReady(player1, new LikenessLooter());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copy ability copies an own-graveyard creature, adds flying, and retains the ability")
    void copiesOwnGraveyardCreatureWithFlyingAndAbility() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(looter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, looter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, looter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, looter, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, 4, giant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(looter.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Copy ability cannot target an opponent's graveyard")
    void copyAbilityTargetsOnlyOwnGraveyard() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(looter.getCard().getName()).isEqualTo("Likeness Looter");
    }
}
