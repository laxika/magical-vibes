package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.ForbiddenOrchard;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KikuNightsFlower.class, IsamaruHoundOfKonda.class, KamiOfOldStone.class, ForbiddenOrchard.class})
class KikuNightsFlowerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability kills a 2/2 which deals 2 damage to itself")
    void killsCreatureWhenPowerIsLethal() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("A 1/7 survives with 1 marked damage")
    void survivesWhenPowerIsBelowToughness() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Kami of Old Stone");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent kami = findPermanent(player2, "Kami of Old Stone");
        assertThat(kami.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Kiku")
    void activationTapsKiku() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Kiku, Night's Flower").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Isamaru, Hound of Konda");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kiku, Night's Flower");
        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID orchardId = harness.getPermanentId(player2, "Forbidden Orchard");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, orchardId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forbidden Orchard");
    }
}
