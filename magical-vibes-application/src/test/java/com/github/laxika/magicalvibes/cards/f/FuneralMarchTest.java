package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FuneralMarchTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    private Permanent attachFuneralMarch(com.github.laxika.magicalvibes.model.Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new FuneralMarch());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("When the enchanted creature leaves, its controller sacrifices a creature of their choice")
    void controllerSacrificesWhenEnchantedCreatureLeaves() {
        Permanent enchanted = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(enchanted);
        gd.playerBattlefields.get(player1.getId()).add(victim);
        attachFuneralMarch(player1, enchanted);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted);
        resolveStack();

        // Only the victim remained after the enchanted creature died, so it is auto-sacrificed.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("The enchanted creature's controller sacrifices, even when a different player controls the Aura")
    void enchantedControllerSacrificesNotAuraController() {
        // Aura controlled by player1, but it enchants player2's creature.
        Permanent enchanted = new Permanent(new GrizzlyBears());
        Permanent player2Victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(enchanted);
        gd.playerBattlefields.get(player2.getId()).add(player2Victim);
        Permanent player1Creature = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(player1Creature);
        attachFuneralMarch(player1, enchanted);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted);
        resolveStack();

        // Player2 (the enchanted creature's controller) sacrifices; player1's board is untouched.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Funeral March cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new Spellbook()));
        harness.setHand(player1, List.of(new FuneralMarch()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }
}
