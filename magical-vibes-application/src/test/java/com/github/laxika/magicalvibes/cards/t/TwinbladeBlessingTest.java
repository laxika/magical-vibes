package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinbladeBlessing.class, GrizzlyBears.class, Mountain.class})
class TwinbladeBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Twinblade Blessing attaches it and grants double strike")
    void resolvingAttachesAndGrantsDoubleStrike() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TwinbladeBlessing
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Twinblade Blessing does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TwinbladeBlessing());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature loses double strike when Twinblade Blessing leaves")
    void losesDoubleStrikeWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TwinbladeBlessing());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Twinblade Blessing cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(findPermanent(player1, "Twinblade Blessing").getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(findPermanent(player1, "Twinblade Blessing").getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void enchantedUnblockedAttackerDealsDamageTwice() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void killingBlockerWithFirstStrikeDoesNotDealDamageToPlayer() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwinbladeBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Twinblade Blessing");
        harness.assertInGraveyard(player1, "Twinblade Blessing");
        assertThat(gd.stack).isEmpty();
    }
}
