package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.d.Dovescape;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillarOfTheParuns.class, AzoriusFirstWing.class, SimicInitiate.class,
        PlaxcasterFrogling.class, AzoriusSignet.class, Dovescape.class})
class PillarOfTheParunsTest extends BaseCardTest {

    @Test
    void tappingAddsManaRestrictedToMulticoloredSpells() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCastsMulticoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player1, List.of(new AzoriusFirstWing()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictedManaCannotCastMonocoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(new SimicInitiate()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesEachColorWithoutUsingTheStack(ManaColor color) {
        var pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfTheParuns());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyMana(color))
                .isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaPaysGenericCostOfMulticoloredSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new PlaxcasterFrogling()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyManaTotal()).isZero();
    }

    @Test
    void restrictedManaCannotCastColorlessSpell() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new AzoriusSignet()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaCannotPayActivatedAbilityCost() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.addToBattlefield(player1, new AzoriusSignet());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCastsHybridNoncreatureSpellPaidWithOneColor() {
        harness.addToBattlefield(player1, new PillarOfTheParuns());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setHand(player1, List.of(new Dovescape()));

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyManaTotal()).isZero();
    }
}
