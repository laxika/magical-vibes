package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasClaim.class, Forest.class, GlorySeeker.class, Mountain.class, Quicksand.class})
class SeasClaimTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted Quicksand loses its printed abilities and produces blue mana")
    void nonbasicLandLosesPrintedAbilities() {
        Permanent quicksand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, quicksand.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quicksand.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.effectiveBasicLandTypes(gd, quicksand)).containsExactly(CardSubtype.ISLAND);
        harness.assertOnBattlefield(player1, "Quicksand");
    }

    @Test
    @DisplayName("Sea's Claim goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        gd.playerBattlefields.get(player2.getId()).remove(forest);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sea's Claim");
        harness.assertInGraveyard(player1, "Sea's Claim");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving Sea's Claim attaches it to the target land")
    void resolvingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SeasClaim
                        && forest.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Sea's Claim can enchant an opponent's land")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land produces blue mana instead of its normal mana")
    void enchantedLandProducesBlueMana() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SeasClaim());
        aura.setAttachedTo(mountain.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land's subtypes are overridden to Island only")
    void enchantedLandSubtypesOverriddenToIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SeasClaim());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Normal mana production resumes when Sea's Claim leaves the battlefield")
    void normalManaResumesWhenAuraLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SeasClaim());
        aura.setAttachedTo(mountain.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Sea's Claim targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, glorySeeker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
