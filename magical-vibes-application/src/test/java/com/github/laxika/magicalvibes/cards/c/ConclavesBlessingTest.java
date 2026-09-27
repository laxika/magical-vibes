package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclavesBlessing.class, CourierHawk.class, BorosSignet.class})
class ConclavesBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+2 for each other creature you control")
    void boostsByOtherControlledCreatures() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        harness.addToBattlefield(player1, new CourierHawk());
        harness.addToBattlefield(player1, new CourierHawk());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConclavesBlessing());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts all creatures controlled by the Aura controller when enchanting an opponent's creature")
    void countsAuraControllersCreatures() {
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        harness.addToBattlefield(player1, new CourierHawk());
        harness.addToBattlefield(player1, new CourierHawk());
        harness.addToBattlefield(player2, new CourierHawk());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConclavesBlessing());
        aura.setAttachedTo(opponentHost.getId());

        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(6);
    }

    @Test
    @DisplayName("Updates the boost when a controlled creature leaves")
    void updatesWhenCreatureLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CourierHawk());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConclavesBlessing());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        harness.addToBattlefield(player1, new CourierHawk());
        harness.addToBattlefield(player1, new BorosSignet());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConclavesBlessing());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        harness.setHand(player1, List.of(new ConclavesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ConclavesBlessing
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new BorosSignet());
        harness.setHand(player1, List.of(new ConclavesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Boros Signet");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
