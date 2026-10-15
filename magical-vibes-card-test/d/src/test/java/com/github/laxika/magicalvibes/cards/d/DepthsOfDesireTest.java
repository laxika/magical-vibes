package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.i.IxallisKeeper;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DepthsOfDesire.class, IxallisKeeper.class, Island.class})
class DepthsOfDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Bounces target creature and creates a Treasure token")
    void bouncesCreatureAndCreatesTreasure() {
        harness.addToBattlefield(player2, new IxallisKeeper());
        UUID targetId = harness.getPermanentId(player2, "Ixalli's Keeper");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Creature bounced
        harness.assertNotOnBattlefield(player2, "Ixalli's Keeper");
        harness.assertInHand(player2, "Ixalli's Keeper");

        // Treasure created
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Treasure sacrifices immediately for one mana of any color")
    void treasureTokenHasManaAbility(ManaColor color) {
        harness.addToBattlefield(player2, new IxallisKeeper());
        UUID targetId = harness.getPermanentId(player2, "Ixalli's Keeper");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new IxallisKeeper()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new IxallisKeeper());
        UUID targetId = harness.getPermanentId(player1, "Ixalli's Keeper");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertInHand(player1, "Ixalli's Keeper");

        // Still creates treasure even when bouncing own creature
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution — no Treasure created")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new IxallisKeeper());
        UUID targetId = harness.getPermanentId(player2, "Ixalli's Keeper");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Depths of Desire");
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner's hand")
    void returnsStolenCreatureToOwner() {
        harness.addToBattlefield(player1, new IxallisKeeper());
        UUID targetId = harness.getPermanentId(player1, "Ixalli's Keeper");
        gd.stolenCreatures.put(targetId, player2.getId());
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertInHand(player2, "Ixalli's Keeper");
        harness.assertNotInHand(player1, "Ixalli's Keeper");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Depths of Desire");
    }

}
