package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TricksOfTheTrade.class, WalkingCorpse.class, Mountain.class})
class TricksOfTheTradeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Tricks of the Trade attaches it and grants +2/+0 and can't be blocked")
    void resolvingGrantsBoostAndEvasion() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new TricksOfTheTrade()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, corpse.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Tricks of the Trade");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(corpse.getId());
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses the boost and evasion when the Aura leaves")
    void grantsRemovedWhenAuraLeaves() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TricksOfTheTrade());
        aura.setAttachedTo(corpse.getId());

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isFalse();
    }

    @Test
    @DisplayName("Can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new TricksOfTheTrade()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, corpse.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new TricksOfTheTrade()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Only the enchanted creature cannot be blocked")
    void onlyEnchantedCreatureCannotBeBlocked() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TricksOfTheTrade()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, blocker, enchanted, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, other, defenders)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple copies stack their boosts and evasion remains when one leaves")
    void multipleAurasStack() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new TricksOfTheTrade(), new TricksOfTheTrade()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castEnchantment(player1, 0, corpse.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, corpse.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Tricks of the Trade"));

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, corpse)).isTrue();
    }
}
