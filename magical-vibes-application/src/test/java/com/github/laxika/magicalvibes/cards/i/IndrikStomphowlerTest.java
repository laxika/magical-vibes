package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndrikStomphowler.class, IronStar.class, AuraOfSilence.class, GrizzlyBears.class})
class IndrikStomphowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IronStar());

        castIndrikStomphowler(artifact);

        harness.assertInGraveyard(player2, "Iron Star");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
    }

    @Test
    @DisplayName("Enters by destroying a target enchantment")
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        castIndrikStomphowler(enchantment);

        harness.assertInGraveyard(player2, "Aura of Silence");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndrikStomphowler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castIndrikStomphowler(Permanent target) {
        harness.setHand(player1, List.of(new IndrikStomphowler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
