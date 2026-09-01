package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandbendersStorm.class, Forest.class, GiantWarthog.class})
class SandbendersStormTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a creature with power 4 or greater")
    void destroysLargeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());

        cast(0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Giant Warthog");
        harness.assertInGraveyard(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Earthbend animates a land and puts three counters on it")
    void earthbendsLand() {
        Permanent land = addForest(player1);

        cast(1, land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An earthbended land returns tapped after it dies")
    void returnsTappedFromGraveyardAfterDeath() {
        Permanent land = addForest(player1);
        cast(1, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(land.getCard().getId()));
        assertThat(gqs.isLand(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("An earthbended land returns tapped after it is exiled")
    void returnsTappedFromExileAfterLeaving() {
        Permanent land = addForest(player1);
        cast(1, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Each mode enforces its own target restriction")
    void modesRejectIllegalTargets() {
        Permanent land = addForest(player2);
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent addForest(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{mode}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
