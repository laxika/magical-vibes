package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Flare;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.s.SuqAtaFirewalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentUnicorn.class, Flare.class, FurnaceOfRath.class, Humility.class, Incinerate.class,
        IronTuskElephant.class, SuqAtaFirewalker.class})
class BenevolentUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Incinerate deals 2 instead of 3 to a player")
    void reducesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Reduces spell damage dealt by its own controller's spells too")
    void reducesOwnControllersSpellDamage() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Incinerate marks only 2 damage on a creature")
    void reducesSpellDamageToCreature() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, elephant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elephant);
        assertThat(elephant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A 1-damage spell is reduced to 0 and the creature survives")
    void oneDamageSpellIsFullyReduced() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, elephant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elephant);
        assertThat(elephant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Activated ability damage is not reduced")
    void doesNotReduceAbilityDamage() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        Permanent firewalker = addCreatureReady(player2, new SuqAtaFirewalker());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(firewalker),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Combat damage is not reduced")
    void doesNotReduceCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        harness.addToBattlefield(player2, new BenevolentUnicorn());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not reduce spell damage after losing its ability")
    void doesNotReduceSpellDamageAfterLosingItsAbility() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.setHand(player1, List.of(new Humility()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Unicorns controlled by different players each reduce spell damage")
    void multipleUnicornsReduceTheSameDamageEvent() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.addToBattlefield(player2, new BenevolentUnicorn());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Multiple reductions cannot turn damage into life gain")
    void multipleUnicornsDoNotReduceDamageBelowZero() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.addToBattlefield(player2, new BenevolentUnicorn());
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Unicorn reduces spell damage dealt to itself")
    void reducesSpellDamageToItself() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new BenevolentUnicorn());
        harness.setHand(player2, List.of(new Flare()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unicorn);
        assertThat(unicorn.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The reduction stops once the Unicorn dies")
    void doesNotReduceDamageAfterLeavingBattlefield() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new BenevolentUnicorn());
        harness.setHand(player2, List.of(new Incinerate(), new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        harness.assertNotOnBattlefield(player1, "Benevolent Unicorn");
        harness.assertInGraveyard(player1, "Benevolent Unicorn");

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The damaged player chooses the order of reduction and doubling")
    void affectedPlayerChoosesReplacementOrder() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
        var choice = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, choice.options().stream()
                .filter(option -> option.startsWith("Benevolent Unicorn")).findFirst().orElseThrow());

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Incinerate");
    }

    @Test
    void affectedPlayerCanDoubleBeforeApplyingReduction() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        var choice = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, choice.options().stream()
                .filter(option -> option.startsWith("Furnace of Rath")).findFirst().orElseThrow());

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Incinerate");
    }

    @Test
    void affectedPlayerCanMultiplyBetweenIndependentReductions() {
        harness.addToBattlefield(player1, new BenevolentUnicorn());
        harness.addToBattlefield(player2, new BenevolentUnicorn());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        var firstChoice = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, firstChoice.options().stream()
                .filter(option -> option.startsWith("Benevolent Unicorn")).findFirst().orElseThrow());
        harness.assertLife(player2, 20);
        var secondChoice = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, secondChoice.options().stream()
                .filter(option -> option.startsWith("Furnace of Rath")).findFirst().orElseThrow());

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Incinerate");
    }
}
