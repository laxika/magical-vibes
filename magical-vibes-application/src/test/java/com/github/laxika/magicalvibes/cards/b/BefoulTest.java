package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Befoul.class, GloriousAnthem.class, GrizzlyBears.class, MassOfGhouls.class, Mountain.class, ScatheZombies.class})
class BefoulTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Befoul destroys a nonblack creature and it can't be regenerated")
    void destroysNonblackCreatureWithoutRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving Befoul destroys a target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, landId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Resolving Befoul destroys a target land")
    void destroysTargetLandUpstreamReview() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Befoul cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Befoul cannot target a black creature")
    void cannotTargetBlackCreatureUpstreamReview() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Befoul cannot target a nonland, noncreature permanent")
    void cannotTargetNonlandNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
