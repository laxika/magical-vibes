package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.n.NezumiCutthroat;
import com.github.laxika.magicalvibes.cards.n.NezumiRonin;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Befoul.class, ConsumingVortex.class, HondenOfCleansingFire.class, KamiOfOldStone.class, NezumiCutthroat.class,
        NezumiRonin.class, Swamp.class})
class BefoulTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Befoul destroys a nonblack creature and it can't be regenerated")
    void destroysNonblackCreatureWithoutRegeneration() {
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        kami.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, kami.getId());

        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Resolving Befoul destroys a colorless land")
    void destroysTargetLand() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, swamp.getId());

        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player2, "Swamp");
    }

    @Test
    @DisplayName("Resolving Befoul destroys a target land through the standard resolution helper")
    void destroysTargetLandUpstreamReview() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, swamp.getId());

        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player2, "Swamp");
    }

    @Test
    @DisplayName("Befoul cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NezumiCutthroat());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Befoul cannot target a black creature")
    void cannotTargetBlackCreatureUpstreamReview() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NezumiRonin());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Befoul cannot target a nonland, noncreature permanent")
    void cannotTargetNonlandNoncreaturePermanent() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player2, new HondenOfCleansingFire());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, shrine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Befoul can destroy its controller's nonblack creature")
    void destroysOwnCreature() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, kami.getId());

        harness.assertNotOnBattlefield(player1, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Befoul prevents a land's regeneration")
    void destroysLandWithoutRegeneration() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        swamp.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, swamp.getId());

        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player2, "Swamp");
    }

    @Test
    @DisplayName("Befoul does not destroy a creature returned to hand in response")
    void doesNotDestroyTargetThatLeftBattlefield() {
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.setHand(player1, List.of(new Befoul()));
        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, kami.getId());
        harness.castAndResolveInstant(player2, 0, kami.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kami of Old Stone");
        harness.assertNotInGraveyard(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Befoul");
    }
}
