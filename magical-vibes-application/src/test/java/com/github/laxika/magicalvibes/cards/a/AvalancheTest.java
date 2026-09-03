package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Avalanche.class, Plains.class, SnowCoveredPlains.class})
class AvalancheTest extends BaseCardTest {

    private Permanent snowLand(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new SnowCoveredPlains());
    }

    private List<UUID> battlefieldIds(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream().map(Permanent::getId).toList();
    }

    @Test
    @DisplayName("X=2 destroys two target snow lands")
    void destroysTwoSnowLands() {
        Permanent s1 = snowLand(player2);
        Permanent s2 = snowLand(player2);
        harness.setHand(player1, List.of(new Avalanche()));
        harness.addMana(player1, ManaColor.RED, 6); // X=2: {2}{2}{R}{R}

        harness.castSorcery(player1, 0, 2, List.of(s1.getId(), s2.getId()));
        harness.passBothPriorities();

        assertThat(battlefieldIds(player2)).doesNotContain(s1.getId(), s2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(s1.getCard().getId(), s2.getCard().getId());
    }

    @Test
    @DisplayName("X=0 destroys nothing")
    void xZeroDestroysNothing() {
        Permanent snow = snowLand(player2);
        harness.setHand(player1, List.of(new Avalanche()));
        harness.addMana(player1, ManaColor.RED, 4); // X=0: {2}{R}{R}

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(battlefieldIds(player2)).contains(snow.getId());
    }

    @Test
    @DisplayName("Requires exactly X snow land targets")
    void requiresExactlyXTargets() {
        Permanent snow = snowLand(player2);
        harness.setHand(player1, List.of(new Avalanche()));
        harness.addMana(player1, ManaColor.RED, 6); // X=2: {2}{2}{R}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(snow.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that stops being snow before resolution is illegal")
    void targetThatStopsBeingSnowBeforeResolutionIsIllegal() {
        Permanent snow = snowLand(player2);
        harness.setHand(player1, List.of(new Avalanche()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1: {2}{R}{R}
        harness.castSorcery(player1, 0, 1, List.of(snow.getId()));

        TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        harness.passBothPriorities();

        assertThat(battlefieldIds(player2)).contains(snow.getId());
    }

    @Test
    @DisplayName("Cannot target a non-snow land")
    void cannotTargetNonSnowLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new Avalanche()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1

        UUID plainsId = plains.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(plainsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow lands");
    }
}
