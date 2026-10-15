package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeliodGodOfTheSun;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayOfDissolution.class, AngelicChorus.class, GrizzlyBears.class, HeliodGodOfTheSun.class})
class RayOfDissolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target enchantment and gains 3 life")
    void destroysTargetEnchantmentAndGainsLife() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RayOfDissolution()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfDissolution()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own enchantment and gain life")
    void destroysOwnEnchantmentAndGainsLife() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new RayOfDissolution()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Angelic Chorus"));

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An indestructible enchantment survives but you still gain life")
    void gainsLifeWhenEnchantmentCannotBeDestroyed() {
        harness.addToBattlefield(player2, new HeliodGodOfTheSun());
        harness.setHand(player1, List.of(new RayOfDissolution()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Heliod, God of the Sun"));

        harness.assertOnBattlefield(player2, "Heliod, God of the Sun");
        harness.assertInGraveyard(player1, "Ray of Dissolution");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is gained when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RayOfDissolution(), new RayOfDissolution()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player2, "Angelic Chorus");

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
