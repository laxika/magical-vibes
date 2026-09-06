package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NettleGuard.class, GiantGrowth.class, ZuranSpellcaster.class,
        LeoninScimitar.class, GloriousAnthem.class, GrizzlyBears.class})
class NettleGuardTest extends BaseCardTest {

    @Test
    void valiantTriggersForYourSpellOnlyOnceEachTurn() {
        Permanent nettleGuard = harness.addToBattlefieldAndReturn(player1, new NettleGuard());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, nettleGuard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nettleGuard.getPowerModifier()).isEqualTo(3);
        assertThat(nettleGuard.getToughnessModifier()).isEqualTo(5);

        harness.castInstant(player1, 0, nettleGuard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nettleGuard.getPowerModifier()).isEqualTo(6);
        assertThat(nettleGuard.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    void valiantTriggersForYourAbility() {
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent nettleGuard = addCreatureReady(player1, new NettleGuard());

        harness.activateAbility(player1, 0, null, nettleGuard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nettleGuard.getToughnessModifier()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Nettle Guard");
    }

    @Test
    void valiantDoesNotTriggerForOpponentsSpell() {
        Permanent nettleGuard = harness.addToBattlefieldAndReturn(player1, new NettleGuard());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, nettleGuard.getId());
        harness.passBothPriorities();

        assertThat(nettleGuard.getPowerModifier()).isEqualTo(3);
        assertThat(nettleGuard.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    void sacrificesToDestroyTargetArtifact() {
        Permanent nettleGuard = addCreatureReady(player1, new NettleGuard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nettle Guard");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void sacrificesToDestroyTargetEnchantment() {
        addCreatureReady(player1, new NettleGuard());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nettle Guard");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void cannotTargetCreatureWithSacrificeAbility() {
        addCreatureReady(player1, new NettleGuard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nettle Guard");
    }
}
