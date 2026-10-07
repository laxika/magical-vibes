package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianColossus;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
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

@CardUsed({SternProctor.class, WornPowerstone.class, GloriousAnthem.class, CoralMerfolk.class, PhyrexianColossus.class})
class SternProctorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target artifact to its owner's hand")
    void etbReturnsArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WornPowerstone()).getId();
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Worn Powerstone");
        harness.assertInHand(player2, "Worn Powerstone");
        harness.assertOnBattlefield(player1, "Stern Proctor");
    }

    @Test
    @DisplayName("ETB returns a target enchantment to its owner's hand")
    void etbReturnsEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB returns a target permanent to its owner's hand when controlled by another player")
    void etbReturnsTargetToItsOwnerHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castSternProctor(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Worn Powerstone");
        harness.assertNotInHand(player1, "Worn Powerstone");
        harness.assertInHand(player2, "Worn Powerstone");
    }

    @Test
    @DisplayName("ETB cannot target a creature that is neither an artifact nor an enchantment")
    void etbRejectsCreatureTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk()).getId();
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Enters normally when there are no legal ETB targets")
    void entersWithoutLegalTargets() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.castFromHand(player1, new SternProctor(), "{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stern Proctor");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Must return an artifact you control when it is the only legal target")
    void mustReturnOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.castFromHand(player1, new SternProctor(), "{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stern Proctor");
        harness.assertNotOnBattlefield(player1, "Worn Powerstone");
        harness.assertInHand(player1, "Worn Powerstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves after Stern Proctor leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WornPowerstone());
        castSternProctor(target.getId());
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SternProctor)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, source));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stern Proctor");
        harness.assertNotOnBattlefield(player2, "Worn Powerstone");
        harness.assertInHand(player2, "Worn Powerstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return another permanent when its target leaves")
    void triggerDoesNotRetarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WornPowerstone());
        harness.addToBattlefield(player2, new GloriousAnthem());
        castSternProctor(target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stern Proctor");
        harness.assertInHand(player2, "Worn Powerstone");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can return an artifact creature")
    void etbReturnsArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianColossus());
        castSternProctor(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Colossus");
        harness.assertInHand(player2, "Phyrexian Colossus");
        harness.assertOnBattlefield(player1, "Stern Proctor");
    }

    private void castSternProctor(UUID targetId) {
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
