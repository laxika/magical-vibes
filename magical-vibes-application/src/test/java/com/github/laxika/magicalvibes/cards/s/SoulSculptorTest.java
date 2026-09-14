package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulSculptor.class, ElvishLyrist.class, ArgothianSwine.class, DarkRitual.class,
        WornPowerstone.class})
class SoulSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a target creature into an enchantment without abilities")
    void turnsTargetCreatureIntoEnchantment() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent lyrist = addCreatureReady(player2, new ElvishLyrist());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, lyrist.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, lyrist)).isFalse();
        assertThat(gqs.isEnchantment(gd, lyrist)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, lyrist).losesAllAbilities()).isTrue();
    }

    @Test
    @DisplayName("The effect ends when any player casts a creature spell")
    void endsWhenAnyPlayerCastsCreatureSpell() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent lyrist = addCreatureReady(player1, new ElvishLyrist());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, lyrist.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ArgothianSwine(), "{3}{G}");

        assertThat(gqs.isCreature(gd, lyrist)).isTrue();
        assertThat(gqs.isEnchantment(gd, lyrist)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, lyrist).losesAllAbilities()).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not end the effect")
    void remainsUntilCreatureSpellCast() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent lyrist = addCreatureReady(player1, new ElvishLyrist());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, lyrist.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gqs.isCreature(gd, lyrist)).isFalse();
        assertThat(gqs.isEnchantment(gd, lyrist)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, lyrist).losesAllAbilities()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, lyrist)).isFalse();
        assertThat(gqs.isEnchantment(gd, lyrist)).isTrue();
    }

    @Test
    @DisplayName("The transformed creature cannot use its abilities")
    void transformedCreatureCannotUseItsAbilities() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent lyrist = addCreatureReady(player1, new ElvishLyrist());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, lyrist.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a creature")
    void canTargetOnlyCreature() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, powerstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
