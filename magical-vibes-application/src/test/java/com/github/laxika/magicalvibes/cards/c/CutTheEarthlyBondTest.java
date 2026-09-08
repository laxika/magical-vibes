package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneFlight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CutTheEarthlyBondTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an enchanted permanent to its owner's hand")
    void returnsEnchantedPermanentToOwnersHand() {
        Permanent creature = addCreature(player2);
        attachAura(creature);
        castAt(creature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an unenchanted permanent")
    void cannotTargetUnenchantedPermanent() {
        Permanent creature = addCreature(player2);
        harness.setHand(player1, List.of(new CutTheEarthlyBond()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted permanent");
    }

    @Test
    @DisplayName("Becomes illegal if the target is no longer enchanted")
    void fizzlesIfTargetIsNoLongerEnchanted() {
        Permanent creature = addCreature(player2);
        Permanent aura = attachAura(creature);
        castAt(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new ArcaneFlight());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new CutTheEarthlyBond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
    }
}
