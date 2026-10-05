package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.f.ForbiddenOrchard;
import com.github.laxika.magicalvibes.cards.g.GratuitousViolence;
import com.github.laxika.magicalvibes.cards.i.IndomitableWill;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
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

@CardUsed({KikuNightsFlower.class, IsamaruHoundOfKonda.class, KamiOfOldStone.class,
        ForbiddenOrchard.class, IndomitableWill.class, GratuitousViolence.class, ConsumingVortex.class})
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
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID orchardId = harness.getPermanentId(player2, "Forbidden Orchard");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, orchardId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forbidden Orchard");
    }

    @Test
    @DisplayName("Kiku can target itself and dies from its own damage")
    void canTargetItself() {
        Permanent kiku = addCreatureReady(player1, new KikuNightsFlower());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, kiku.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kiku, Night's Flower");
        harness.assertInGraveyard(player1, "Kiku, Night's Flower");
    }

    @Test
    @DisplayName("The damage uses the target's power at resolution")
    void usesPowerAtResolution() {
        addCreatureReady(player1, new KikuNightsFlower());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, kami.getId());

        harness.setHand(player2, List.of(new IndomitableWill()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, kami.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kami of Old Stone");
        assertThat(kami.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A summoning-sick Kiku cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new KikuNightsFlower());
        harness.addToBattlefield(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Kami of Old Stone");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability still resolves after Kiku leaves the battlefield")
    void resolvesWithoutKikuOnBattlefield() {
        Permanent kiku = addCreatureReady(player1, new KikuNightsFlower());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, kami.getId());

        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, kiku.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kiku, Night's Flower");
        assertThat(kami.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability deals no damage when its target leaves before resolution")
    void doesNotDamageTargetThatLeftBattlefield() {
        addCreatureReady(player1, new KikuNightsFlower());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, kami.getId());

        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, kami.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kami of Old Stone");
        harness.assertNotInGraveyard(player2, "Kami of Old Stone");
        assertThat(kami.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kiku's controller's Gratuitous Violence does not double an opponent's self-damage")
    void sourceModifiersUseTargetsController() {
        addCreatureReady(player1, new KikuNightsFlower());
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        assertThat(kami.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gratuitous Violence doubles a friendly target's self-damage only once")
    void appliesSourceDamageReplacementOnlyOnce() {
        addCreatureReady(player1, new KikuNightsFlower());
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        harness.addToBattlefield(player1, new GratuitousViolence());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        assertThat(kami.getMarkedDamage()).isEqualTo(2);
    }
}
