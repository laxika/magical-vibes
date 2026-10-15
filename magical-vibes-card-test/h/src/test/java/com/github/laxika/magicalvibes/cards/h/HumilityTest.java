package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CloudchaserEagle;
import com.github.laxika.magicalvibes.cards.e.ElvishFury;
import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SkyshroudElf;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Humility.class, AirElemental.class, ProdigalSorcerer.class, Opalescence.class,
        CloudchaserEagle.class, ElvishFury.class, SkyshroudElf.class, Flight.class})
class HumilityTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures both players control become base 1/1 and lose their keywords")
    void allCreaturesBecomeVanillaOneOnes() {
        Permanent ownElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent enemyElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        resolveHumility();

        assertThat(gqs.getEffectivePower(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enemyElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enemyElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Humility resolved are also neutered")
    void laterCreaturesAreAlsoNeutered() {
        resolveHumility();
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("+1/+1 counters still apply on top of the 1/1 base")
    void countersApplyOnTopOfBase() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveHumility();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("An activated ability of a creature can no longer be activated")
    void creatureActivatedAbilityIsStripped() {
        addCreatureReady(player1, new ProdigalSorcerer());
        resolveHumility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures return to normal once Humility leaves the battlefield")
    void effectEndsWhenHumilityLeaves() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent humility = resolveHumility();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(humility);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Humility affects itself when Opalescence makes it a creature")
    void animatedHumilityIsAlsoAffected() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent humility = harness.addToBattlefieldAndReturn(player1, new Humility());

        assertThat(gqs.isCreature(gd, humility)).isTrue();
        assertThat(gqs.getEffectivePower(gd, humility)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, humility)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animated Humility continues setting ordinary creatures to 1/1")
    void animatedHumilityStillAffectsOtherCreatures() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new Humility());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Later Opalescence sets Humility to 4/4 but ordinary creatures remain 1/1")
    void laterOpalescenceWinsOnlyForAnimatedEnchantments() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent humility = harness.addToBattlefieldAndReturn(player1, new Humility());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, humility)).isTrue();
        assertThat(gqs.getEffectivePower(gd, humility)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, humility)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature mana abilities are also removed")
    void creatureManaAbilitiesAreStripped() {
        addCreatureReady(player1, new SkyshroudElf());
        resolveHumility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures entering under Humility do not trigger their enters abilities")
    void enteringCreatureDoesNotTrigger() {
        Permanent humility = resolveHumility();
        harness.enterBattlefieldAndReturn(player1, new CloudchaserEagle());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(humility);
    }

    @Test
    @DisplayName("An ability already on the stack resolves after Humility removes it")
    void abilityOnStackSurvivesAbilityRemoval() {
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.addToBattlefield(player2, new Humility());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell bonuses apply on top of Humility's 1/1 base")
    void spellBonusAppliesOnTopOfBase() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        resolveHumility();
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, elemental.getId());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A later Aura can grant a creature flying under Humility")
    void laterAbilityGrantApplies() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        resolveHumility();
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sorcerer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sorcerer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Humility removes flying granted by an earlier Aura")
    void earlierAbilityGrantIsRemoved() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, sorcerer.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.FLYING)).isTrue();

        resolveHumility();

        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.FLYING)).isFalse();
    }

    /** Casts and resolves Humility for player1, returning the resulting battlefield permanent. */
    private Permanent resolveHumility() {
        harness.castFromHand(player1, new Humility(), "{2}{W}{W}");
        harness.passBothPriorities();

        return findPermanent(player1, "Humility");
    }
}
