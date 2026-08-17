package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Skyserpent Seeker")
class SkyserpentSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts the revealed lands onto the battlefield tapped and grows Skyserpent Seeker")
    void exhaustPutsLandsTappedAndAddsCounter() {
        Permanent seeker = addSeeker();
        Shock shock = new Shock();
        Forest forest = new Forest();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, island, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard().getId().equals(island.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("An exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addSeeker();
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addSeeker() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SkyserpentSeeker());
        seeker.setSummoningSick(false);
        return seeker;
    }
}
