package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.p.PutridWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulPresence.class, PutridWarrior.class, DegaDisciple.class, PhyrexianArena.class})
class FoulPresenceTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsMinusOneMinusOne() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        addAttachedAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(1);
    }

    @Test
    void auraDoesNotAffectOtherCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        Permanent otherCreature = addCreatureReady(player1, new PutridWarrior());
        addAttachedAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    void enchantedCreatureCanTapToGiveTargetCreatureMinusOneMinusOneUntilEndOfTurn() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        addAttachedAura(enchantedCreature);
        Permanent targetCreature = addCreatureReady(player2, new PutridWarrior());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(1);
        assertThat(enchantedCreature.isTapped()).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(2);
    }

    @Test
    void grantedMinusOneMinusOneCanKillOneToughnessCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        addAttachedAura(enchantedCreature);
        Permanent targetCreature = addCreatureReady(player2, new DegaDisciple());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dega Disciple");
        harness.assertInGraveyard(player2, "Dega Disciple");
    }

    @Test
    void grantedAbilityCannotTargetNoncreaturePermanent() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        addAttachedAura(enchantedCreature);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void abilityIsLostWhenAuraLeavesTheBattlefield() {
        Permanent enchantedCreature = addCreatureReady(player1, new PutridWarrior());
        Permanent aura = addAttachedAura(enchantedCreature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new PutridWarrior());
        harness.setHand(player1, List.of(new FoulPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Foul Presence");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.setHand(player1, List.of(new FoulPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enchantedOpponentCreatureCanUseGrantedAbility() {
        Permanent enchantedCreature = addCreatureReady(player2, new PutridWarrior());
        addAttachedAura(enchantedCreature);
        Permanent targetCreature = addCreatureReady(player1, new PutridWarrior());

        harness.activateAbility(player2, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(1);
        assertThat(enchantedCreature.isTapped()).isTrue();
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FoulPresence());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
