package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnchantmentAlterationTest extends BaseCardTest {

    @Test
    @DisplayName("Moves an Aura from one creature to another creature")
    void movesAuraFromCreatureToAnotherCreature() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), host);
        castEnchantmentAlteration(List.of(aura.getId(), destination.getId()));

        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Moves an Aura from one land to another land")
    void movesAuraFromLandToAnotherLand() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = addAuraAttachedTo(player1, new Erosion(), host);
        castEnchantmentAlteration(List.of(aura.getId(), destination.getId()));

        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Requires the destination to be another permanent of the Aura host type")
    void rejectsDestinationOfDifferentTypeAndCurrentHost() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), host);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> castEnchantmentAlteration(List.of(aura.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> castEnchantmentAlteration(List.of(aura.getId(), host.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEnchantmentAlteration(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetIds);
    }

    private Permanent addAuraAttachedTo(Player player, Card auraCard, Permanent host) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
