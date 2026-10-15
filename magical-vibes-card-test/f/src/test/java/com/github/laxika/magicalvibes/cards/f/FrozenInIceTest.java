package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrozenInIce.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class, Twiddle.class})
class FrozenInIceTest extends BaseCardTest {

    @Test
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castFrozenInIce(creature);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FrozenInIce
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void enchantedCreatureLosesAbilities() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachAura(creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void removingAuraRestoresAbilities() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        Permanent aura = attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FrozenInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void fizzlesIfTargetIsRemovedBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FrozenInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Frozen in Ice");
        harness.assertNotOnBattlefield(player1, "Frozen in Ice");
    }

    private void castFrozenInIce(Permanent target) {
        harness.setHand(player1, List.of(new FrozenInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent attachAura(Permanent target) {
        Permanent aura = new Permanent(new FrozenInIce());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    void enchantedCreatureCannotBeUntappedByASpell() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachAura(creature);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void removingAuraAllowsCreatureToUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = attachAura(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void canEnchantOwnCreatureWithoutAffectingOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new AirElemental());
        Permanent otherCreature = addCreatureReady(player1, new AirElemental());

        castFrozenInIce(creature);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
    }
}
