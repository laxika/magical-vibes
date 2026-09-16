package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkwaterEgg;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.j.JunkGolem;
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

@CardUsed({BashToBits.class, DarkwaterEgg.class, DuskImp.class, JunkGolem.class})
class BashToBitsTest extends BaseCardTest {

    @Test
    @DisplayName("Bash to Bits destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new DarkwaterEgg());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Darkwater Egg");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Darkwater Egg");
        harness.assertInGraveyard(player2, "Darkwater Egg");
        harness.assertInGraveyard(player1, "Bash to Bits");
    }

    @Test
    @DisplayName("Bash to Bits cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new DuskImp());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Dusk Imp");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bash to Bits can target an artifact creature")
    void canTargetArtifactCreature() {
        harness.enterBattlefieldAndReturn(player2, new JunkGolem());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Junk Golem");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Junk Golem");
        harness.assertInGraveyard(player2, "Junk Golem");
    }

    @Test
    @DisplayName("Flashback destroys target artifact and exiles Bash to Bits")
    void flashbackDestroysArtifactAndExilesSpell() {
        harness.addToBattlefield(player2, new DarkwaterEgg());
        harness.setGraveyard(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Darkwater Egg");
        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Darkwater Egg");
        harness.assertInGraveyard(player2, "Darkwater Egg");
        harness.assertNotInGraveyard(player1, "Bash to Bits");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bash to Bits"));
    }

    @Test
    @DisplayName("Flashback exiles Bash to Bits when its target is gone")
    void flashbackExilesWhenTargetIsGone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarkwaterEgg());
        harness.setGraveyard(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Bash to Bits");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bash to Bits"));
    }

    @Test
    @DisplayName("Bash to Bits cannot be cast with an incomplete flashback cost")
    void flashbackRequiresFullCost() {
        harness.addToBattlefield(player2, new DarkwaterEgg());
        harness.setGraveyard(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Darkwater Egg");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
