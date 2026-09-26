package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.n.NezumiCutthroat;
import com.github.laxika.magicalvibes.cards.n.NezumiRonin;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Befoul.class, HondenOfCleansingFire.class, KamiOfOldStone.class, NezumiCutthroat.class,
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
    @DisplayName("Resolving Befoul destroys a target land, including a black land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID landId = harness.getPermanentId(player2, "Swamp");
        harness.castAndResolveSorcery(player1, 0, 0, landId);

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
}
