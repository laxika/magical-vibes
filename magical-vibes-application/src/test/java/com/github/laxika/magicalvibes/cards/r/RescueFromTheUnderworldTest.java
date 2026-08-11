package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RescueFromTheUnderworldTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and the sacrificed creature at the next upkeep")
    void returnsTargetAndSacrificedCreatureAtNextUpkeep() {
        Card target = new GrizzlyBears();
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        RescueFromTheUnderworld rescue = new RescueFromTheUnderworld();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(rescue));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(rescue.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(target.getId(), sacrificed.getCard().getId());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(target.getId(), sacrificed.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId(), sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("Returns the sacrificed creature if the target card leaves the graveyard")
    void returnsRemainingCardIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        RescueFromTheUnderworld rescue = new RescueFromTheUnderworld();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(rescue));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();
        gd.playerGraveyards.get(player1.getId()).remove(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(sacrificed.getCard().getId());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(target.getId())
                .contains(sacrificed.getCard().getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new HolyDay();
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new RescueFromTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, noncreature.getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
    }
}
