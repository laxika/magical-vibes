package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        StrengthOfUnity.class, BenalishLancer.class, Forest.class, Island.class,
        Mountain.class, Plains.class, Swamp.class
})
class StrengthOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each distinct basic land type")
    void boostsByDomainCount() {
        Permanent lancer = enchantedLancer();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Domain counts distinct types controlled by the Aura controller")
    void countsDistinctControllerTypesOnly() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player2, new BenalishLancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfUnity());
        aura.setAttachedTo(lancer.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus updates when controlled land types change")
    void updatesDynamicallyWithLandTypes() {
        Permanent lancer = enchantedLancer();

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(2);
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(4);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Domain counts all five basic land types")
    void countsAllBasicLandTypes() {
        Permanent lancer = enchantedLancer();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(7);
    }

    @Test
    @DisplayName("Casting Strength of Unity attaches it to the targeted creature")
    void castsAndAttachesToCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new StrengthOfUnity()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, lancer.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Strength of Unity");
        assertThat(aura.getAttachedTo()).isEqualTo(lancer.getId());
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus ends when Strength of Unity leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent lancer = enchantedLancer();
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        Permanent aura = findPermanent(player1, "Strength of Unity");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StrengthOfUnity()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent forest = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchantedLancer() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfUnity());
        aura.setAttachedTo(lancer.getId());
        return lancer;
    }
}
