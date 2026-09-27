package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnchantresssPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DismantlingWave.class, EnchantresssPresence.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class DismantlingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to one artifact or enchantment controlled by the opponent")
    void destroysTargetedOpponentPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new DismantlingWave()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Can destroy no targets")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new DismantlingWave()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dismantling Wave");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its caster")
    void cannotTargetOwnPermanent() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new DismantlingWave()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling destroys all artifacts and enchantments before drawing")
    void cyclingWipesArtifactsAndEnchantmentsAndDraws() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new EnchantresssPresence());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new EnchantresssPresence());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DismantlingWave()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Enchantress's Presence");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Enchantress's Presence");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Dismantling Wave");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
