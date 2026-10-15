package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pillage.class, Millstone.class, Mountain.class, GrizzlyBears.class, Boomerang.class})
class PillageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Pillage destroys a target artifact and it can't be regenerated")
    void destroysTargetArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        millstone.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, millstone.getId());

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Resolving Pillage destroys a target land")
    void destroysTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Pillage can destroy a target land controlled by its caster")
    void destroysOwnLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Pillage cannot target a nonartifact creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pillage can destroy an artifact controlled by its caster")
    void destroysOwnArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, millstone.getId());

        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("Pillage does not destroy another permanent when its target leaves the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new Pillage()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, mountain.getId());
        harness.castAndResolveInstant(player2, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player1, "Pillage");
        assertThat(gd.stack).isEmpty();
    }
}
