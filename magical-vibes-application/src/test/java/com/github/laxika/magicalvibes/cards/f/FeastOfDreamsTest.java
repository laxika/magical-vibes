package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalefulEidolon;
import com.github.laxika.magicalvibes.cards.c.CripplingBlight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeastOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an enchanted creature")
    void destroysEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new CripplingBlight());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        castFeastOfDreams(creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys an enchantment creature")
    void destroysEnchantmentCreature() {
        Permanent eidolon = harness.addToBattlefieldAndReturn(player2, new BalefulEidolon());

        castFeastOfDreams(eidolon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Baleful Eidolon");
        harness.assertInGraveyard(player2, "Baleful Eidolon");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither enchanted nor an enchantment")
    void cannotTargetOrdinaryCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeastOfDreams()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted creature or an enchantment creature");
    }

    private void castFeastOfDreams(UUID targetId) {
        harness.setHand(player1, List.of(new FeastOfDreams()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);
    }
}
