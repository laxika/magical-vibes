package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfTerribleSecrets.class, DarksteelCitadel.class, PhyrexianArena.class, Forest.class})
class KamiOfTerribleSecretsTest extends BaseCardTest {

    @Test
    void drawsAndGainsLifeWhenControllerHasArtifactAndEnchantment() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new PhyrexianArena());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new KamiOfTerribleSecrets()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 21);
    }

    @Test
    void doesNothingWithoutArtifact() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new KamiOfTerribleSecrets()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNothingWithoutEnchantment() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new KamiOfTerribleSecrets()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
