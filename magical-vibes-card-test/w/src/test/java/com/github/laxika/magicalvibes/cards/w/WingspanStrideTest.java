package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FortressKinGuard;
import com.github.laxika.magicalvibes.cards.m.MoxJasper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingspanStride.class, MoxJasper.class, FortressKinGuard.class})
class WingspanStrideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent guard = addCreatureReady(player1, new FortressKinGuard());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingspanStride());
        aura.setAttachedTo(guard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wingspan Stride returns to its owner's hand for {2}{U}")
    void activatedAbilityReturnsAuraToHand() {
        Permanent guard = addCreatureReady(player1, new FortressKinGuard());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingspanStride());
        aura.setAttachedTo(guard.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wingspan Stride");
        harness.assertNotOnBattlefield(player1, "Wingspan Stride");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Resolving Wingspan Stride attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new FortressKinGuard());
        harness.setHand(player1, List.of(new WingspanStride()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, guard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Wingspan Stride")
                        && permanent.getAttachedTo().equals(guard.getId()));
    }

    @Test
    @DisplayName("Wingspan Stride cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new MoxJasper());
        harness.setHand(player1, List.of(new WingspanStride()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = findPermanent(player1, "Mox Jasper");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without boosting other creatures")
    void canEnchantOpponentsCreature() {
        Permanent ownGuard = harness.addToBattlefieldAndReturn(player1, new FortressKinGuard());
        Permanent opposingGuard = harness.addToBattlefieldAndReturn(player2, new FortressKinGuard());
        harness.setHand(player1, List.of(new WingspanStride()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, opposingGuard.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wingspan Stride").getAttachedTo()).isEqualTo(opposingGuard.getId());
        assertThat(gqs.getEffectivePower(gd, opposingGuard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingGuard, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownGuard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGuard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownGuard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura controlled by another player returns to its owner, leaving its host in play")
    void returnsToOwnerRatherThanController() {
        Permanent guard = harness.addToBattlefieldAndReturn(player2, new FortressKinGuard());
        WingspanStride card = new WingspanStride();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(guard.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wingspan Stride");
        harness.assertNotInHand(player2, "Wingspan Stride");
        harness.assertNotOnBattlefield(player2, "Wingspan Stride");
        harness.assertOnBattlefield(player2, "Fortress Kin-Guard");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Returning the Aura requires blue mana as well as the generic cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new FortressKinGuard());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingspanStride());
        aura.setAttachedTo(guard.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wingspan Stride");
        harness.assertNotInHand(player1, "Wingspan Stride");
        assertThat(gd.stack).isEmpty();
    }
}
