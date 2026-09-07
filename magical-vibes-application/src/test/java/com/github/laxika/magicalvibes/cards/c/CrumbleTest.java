package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crumble.class, RodOfRuin.class, GrizzlyBears.class})
class CrumbleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crumble destroys target artifact and its controller gains life equal to its mana value")
    void destroysArtifactAndControllerGainsLife() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int ownerLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        // Rod of Ruin has mana value 4; its controller (player2), not the caster, gains 4 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore + 4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Crumble destroys an artifact despite a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Crumble does not destroy an indestructible artifact, but its controller gains life")
    void gainsLifeWhenArtifactIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.assertLife(player2, controllerLifeBefore + 4);
        harness.assertLife(player1, casterLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature with Crumble")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
