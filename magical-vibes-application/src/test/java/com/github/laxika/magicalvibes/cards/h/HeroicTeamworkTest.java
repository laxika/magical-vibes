package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroicTeamwork.class, AirElemental.class, GrizzlyBears.class, Island.class})
class HeroicTeamworkTest extends BaseCardTest {

    @Test
    @DisplayName("Gives one or two target creatures +2/+1 until end of turn")
    void boostsOneOrTwoCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()), List.of());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Teamwork taps creatures with total power three and draws a card")
    void teamworkTapsCreaturesAndDraws() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teamworkCreature = addCreatureReady(player1, new AirElemental());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(List.of(target.getId()), List.of(teamworkCreature.getId()));

        assertThat(teamworkCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Declining teamwork does not draw a card or tap creatures")
    void decliningTeamworkDoesNotDrawOrTap() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teamworkCreature = addCreatureReady(player1, new AirElemental());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        cast(List.of(target.getId()), List.of());

        assertThat(teamworkCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new HeroicTeamwork()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(List<UUID> targetIds, List<UUID> teamworkIds) {
        harness.setHand(player1, List.of(new HeroicTeamwork()));
        addMana();
        if (teamworkIds.isEmpty()) {
            harness.castInstant(player1, 0, targetIds);
        } else {
            harness.castInstantWithSacrifices(player1, 0, targetIds.get(0), teamworkIds);
        }
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
