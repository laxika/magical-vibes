package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({NahirisLithoforming.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class NahirisLithoformingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices up to X lands, draws for the actual number, and grants X land plays")
    void resolvesAllEffectsFromTheAnnouncedX() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Mountain()),
                harness.addToBattlefieldAndReturn(player1, new Mountain()),
                harness.addToBattlefieldAndReturn(player1, new Mountain()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());

        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId(), lands.get(1).getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, lands.get(2));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lands.get(0), lands.get(1));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);

        Permanent ownLand = harness.enterBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingLand = harness.enterBattlefieldAndReturn(player2, new Mountain());
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isFalse();
    }
}
