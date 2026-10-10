package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({DeflectingSwat.class, Boomerang.class, EdgarMarkov.class, GrizzlyBears.class, IcyManipulator.class, ProdigalPyromancer.class, Shock.class, SeedsOfStrength.class})
class DeflectingSwatTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast without paying its mana cost while controlling a commander")
    void freeCastWhileControllingCommander() {
        addCommanderToBattlefield(player2);
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent alternateTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new DeflectingSwat()));

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, boomerang.getId(), List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, alternateTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(alternateTarget.getId()));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the free alternate cost without controlling a commander")
    void freeCastRequiresCommander() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, shock.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can change the target of a single-target activated ability")
    void retargetsActivatedAbility() {
        IcyManipulator icyManipulator = new IcyManipulator();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent alternateTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, icyManipulator);
        addCommanderToBattlefield(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new DeflectingSwat()));

        harness.activateAbility(player1, 1, null, originalTarget.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, icyManipulator.getId(), List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, alternateTarget.getId());
        harness.passBothPriorities();

        assertThat(originalTarget.isTapped()).isFalse();
        assertThat(alternateTarget.isTapped()).isTrue();
    }

    private void addCommanderToBattlefield(com.github.laxika.magicalvibes.model.Player player) {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }
    @Test
    void changesTargetOfSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void changesTargetOfActivatedAbility() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, pyromancer.getCard().getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player2, commander);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.castInstantWithAlternateCost(player2, 0, shock.getId(), List.of());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        gd.playerCommandZones.get(player2.getId()).add(commander);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, shock.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllingOpponentsCommanderAllowsFreeCast() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Permanent stolenCommander = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), stolenCommander,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.castInstantWithAlternateCost(player2, 0, shock.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void decliningRetargetKeepsOriginalTarget() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void mayRetargetToCreatureAlreadyTargetedByAnotherInstruction() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SeedsOfStrength seeds = new SeedsOfStrength();
        harness.setHand(player1, List.of(seeds));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId(), second.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, seeds.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(second.getId());
        harness.handlePermanentChosen(player2, second.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
    }

    @Test
    void mayKeepFirstTargetAndChangeOnlyLaterTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SeedsOfStrength seeds = new SeedsOfStrength();
        harness.setHand(player1, List.of(seeds));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new DeflectingSwat()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, seeds.getId());

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(second.getId())
                .contains(replacement.getId());
        harness.handlePermanentChosen(player2, replacement.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
    }

}
