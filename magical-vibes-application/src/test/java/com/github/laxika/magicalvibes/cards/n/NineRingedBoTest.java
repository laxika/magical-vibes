package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NineRingedBo.class, KamiOfOldStone.class, LanternKami.class, DevotedRetainer.class, RendSpirit.class})
class NineRingedBoTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a 1-toughness Spirit and exiles it instead of putting it into the graveyard")
    void killsAndExilesSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lantern Kami");
        harness.assertNotInGraveyard(player2, "Lantern Kami");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lantern Kami"));
    }

    @Test
    @DisplayName("Marks a surviving Spirit so a later death this turn exiles it")
    void marksSurvivingSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        assertThat(kami.getMarkedDamage()).isEqualTo(1);
        assertThat(kami.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Exiles a marked Spirit destroyed by another effect later this turn")
    void exilesSpiritDestroyedLaterThisTurn() {
        harness.addToBattlefield(player1, new NineRingedBo());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, kami.getId());

        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertNotInGraveyard(player2, "Kami of Old Stone");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Kami of Old Stone"));
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevotedRetainer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }
}
