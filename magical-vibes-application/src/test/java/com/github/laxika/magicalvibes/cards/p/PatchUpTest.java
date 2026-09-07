package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatchUp.class, GrizzlyBears.class, LlanowarElves.class, Memnite.class, Ornithopter.class,
        HillGiant.class})
class PatchUpTest extends BaseCardTest {

    @Test
    void returnsUpToThreeCreaturesWithinTotalManaValueLimit() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card memnite = new Memnite();
        Card ornithopter = new Ornithopter();
        Card hillGiant = new HillGiant();
        Card patchUp = new PatchUp();
        harness.setGraveyard(player1, List.of(bears, elves, memnite, ornithopter, hillGiant));
        harness.setHand(player1, List.of(patchUp));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.maxTotalManaValue()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactly(
                bears.getId(), elves.getId(), memnite.getId(), ornithopter.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId(), memnite.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bears.getId(), elves.getId(), memnite.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(ornithopter.getId(), hillGiant.getId(), patchUp.getId());
    }

    @Test
    void rejectsTargetsOverTotalManaValueLimit() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears, secondBears));
        harness.setHand(player1, List.of(new PatchUp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
    }
}
