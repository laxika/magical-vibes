package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishHerder;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SkyshroudElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlandWeavemaster.class, LlanowarElves.class, GrizzlyBears.class,
        SkyshroudElf.class, FountainOfYouth.class, ElvishHerder.class})
class WoodlandWeavemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Elf you control enters")
    void boostsWhenElfEnters() {
        Permanent weavemaster = addReadyWeavemaster();
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, weavemaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, weavemaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost itself when it enters")
    void doesNotBoostItselfWhenEntering() {
        Permanent weavemaster = harness.addToBattlefieldAndReturn(player1, new WoodlandWeavemaster());

        assertThat(gqs.getEffectivePower(gd, weavemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, weavemaster)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost when a non-Elf creature enters")
    void doesNotBoostWhenNonElfEnters() {
        Permanent weavemaster = addReadyWeavemaster();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, weavemaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, weavemaster)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping adds mana equal to current power in one chosen color")
    void tappingAddsManaEqualToCurrentPower() {
        Permanent weavemaster = addReadyWeavemaster();
        weavemaster.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELF), ManaColor.BLUE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Restricted mana can be spent to cast an Elf spell")
    void restrictedManaCanCastElfSpell() {
        addRestrictedMana(1);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot be spent to cast a non-Elf spell")
    void restrictedManaCannotCastNonElfSpell() {
        addRestrictedMana(1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of an Elf source")
    void restrictedManaCanActivateElfSourceAbility() {
        addRestrictedMana(1);
        harness.addToBattlefield(player1, new SkyshroudElf());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot activate an ability of a non-Elf source")
    void restrictedManaCannotActivateNonElfSourceAbility() {
        addRestrictedMana(2);
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        fountain.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can activate an ability of an Elf source with a target")
    void restrictedManaCanActivateTargetedElfSourceAbility() {
        addRestrictedMana(1);
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new ElvishHerder());

        harness.activateAbility(player1, 1, 0, null, herder.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, herder, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyWeavemaster() {
        Permanent weavemaster = harness.addToBattlefieldAndReturn(player1, new WoodlandWeavemaster());
        weavemaster.setSummoningSick(false);
        return weavemaster;
    }

    private void addRestrictedMana(int amount) {
        Permanent weavemaster = addReadyWeavemaster();
        if (amount > 1) {
            weavemaster.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, amount - 1);
        }
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
    }
}
