package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevoutHarpistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an Aura attached to a creature")
    void destroysAuraAttachedToCreature() {
        addReadyHarpist(player1);
        Permanent creature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, creature);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertInGraveyard(player2, "Pacifism");
    }

    @Test
    @DisplayName("Cannot target an Aura that is not attached to a creature")
    void cannotTargetUnattachedAura() {
        addReadyHarpist(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyHarpist(player1);
        Permanent creature = addCreatureReady(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHarpist(Player player) {
        Permanent harpist = harness.addToBattlefieldAndReturn(player, new DevoutHarpist());
        harpist.setSummoningSick(false);
        return harpist;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
