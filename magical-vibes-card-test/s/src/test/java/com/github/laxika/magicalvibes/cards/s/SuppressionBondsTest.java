package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Meteorite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuppressionBonds.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, Meteorite.class})
class SuppressionBondsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Suppression Bonds attaches it to the target permanent")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuppressionBonds()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Suppression Bonds")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent bonds = harness.addToBattlefieldAndReturn(player2, new SuppressionBonds());
        bonds.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent bonds = harness.addToBattlefieldAndReturn(player1, new SuppressionBonds());
        bonds.setAttachedTo(blocker.getId());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted noncreature permanent cannot activate its abilities")
    void enchantedArtifactCannotActivateAbilities() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        Permanent bonds = harness.addToBattlefieldAndReturn(player2, new SuppressionBonds());
        bonds.setAttachedTo(fountain.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Suppression Bonds cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SuppressionBonds()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Suppression Bonds resolves on an artifact and prevents its mana ability")
    void enchantedArtifactCannotActivateManaAbility() {
        Permanent meteorite = harness.addToBattlefieldAndReturn(player2, new Meteorite());
        harness.setHand(player1, List.of(new SuppressionBonds()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, meteorite.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Suppression Bonds").getAttachedTo()).isEqualTo(meteorite.getId());
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(meteorite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated abilities become available again when Suppression Bonds leaves")
    void removingBondsRestoresActivatedAbilities() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent bonds = harness.addToBattlefieldAndReturn(player2, new SuppressionBonds());
        bonds.setAttachedTo(fountain.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        gd.playerBattlefields.get(player2.getId()).remove(bonds);
        gd.playerGraveyards.get(player2.getId()).add(bonds.getCard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Suppression Bonds goes to the graveyard if its target leaves before resolution")
    void targetLeavingPreventsAttachment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuppressionBonds()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Suppression Bonds");
        harness.assertInGraveyard(player1, "Suppression Bonds");
        assertThat(gd.stack).isEmpty();
    }
}
