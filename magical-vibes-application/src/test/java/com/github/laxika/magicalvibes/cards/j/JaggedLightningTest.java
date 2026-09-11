package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({JaggedLightning.class, BlanchwoodTreefolk.class, CoralMerfolk.class, Mountain.class, Rescind.class})
class JaggedLightningTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {3}{R}{R}
    }

    @Test
    @DisplayName("Deals 3 damage to each of two target creatures, destroying both 2/1s")
    void destroysBothTargets() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(a.getId(), b.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Coral Merfolk")).hasSize(2);
    }

    @Test
    @DisplayName("Each target takes 3 damage independently; a 4/5 survives with damage marked")
    void marksDamageOnSurvivor() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new BlanchwoodTreefolk());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(treefolk.getId(), merfolk.getId()));

        assertThat(treefolk.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Blanchwood Treefolk");
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Requires two targets; casting with a single target is rejected")
    void requiresTwoTargets() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        List<UUID> single = List.of(merfolk.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, single))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        List<UUID> duplicate = List.of(merfolk.getId(), merfolk.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, duplicate))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target one creature controlled by each player")
    void targetsCreaturesControlledByEitherPlayer() {
        Permanent ownTreefolk = harness.addToBattlefieldAndReturn(player1, new BlanchwoodTreefolk());
        Permanent opposingMerfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(ownTreefolk.getId(), opposingMerfolk.getId()));

        assertThat(ownTreefolk.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Blanchwood Treefolk");
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Still damages a legal target when the other target leaves before resolution")
    void stillResolvesWithOneIllegalTarget() {
        Permanent returnedMerfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent survivingTreefolk = harness.addToBattlefieldAndReturn(player2, new BlanchwoodTreefolk());
        harness.setHand(player1, List.of(new JaggedLightning()));
        harness.setHand(player2, List.of(new Rescind()));
        giveMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(returnedMerfolk.getId(), survivingTreefolk.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, returnedMerfolk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Coral Merfolk");
        assertThat(survivingTreefolk.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Blanchwood Treefolk");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.addToBattlefield(player1, new Mountain());
        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        List<UUID> targets = List.of(creature.getId(), mountainId);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }
}
