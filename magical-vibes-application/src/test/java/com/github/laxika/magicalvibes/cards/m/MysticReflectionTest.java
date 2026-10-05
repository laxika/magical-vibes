package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbsorbIdentity;
import com.github.laxika.magicalvibes.cards.a.AlrundsEpiphany;
import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.cards.n.NikoAris;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticReflection.class, Archangel.class, GrizzlyBears.class,
        AbsorbIdentity.class, AlrundsEpiphany.class, IcehideTroll.class, Mistwalker.class,
        MoritteOfTheFrost.class, NikoAris.class, RavenousLindwurm.class})
class MysticReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("The next creature enters as a copy of the targeted creature")
    void nextCreatureEntersAsTargetCopy() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));

        Permanent bears = castBears();

        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
        assertThat(bears.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The replacement is consumed after one entry event")
    void replacementIsConsumedAfterOneEntryEvent() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));

        Permanent firstBears = castBears();
        Permanent secondBears = castBears();

        assertThat(firstBears.getCard().getName()).isEqualTo("Archangel");
        assertThat(secondBears.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A target that leaves the battlefield is still copied using last-known information")
    void targetLeavingBattlefieldStillProvidesCopyValues() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Archangel"));

        Permanent bears = castBears();

        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
    }

    @Test
    void allCreaturesInOneTokenEventEnterAsCopies() {
        harness.addToBattlefield(player1, new IcehideTroll());
        castMysticReflection(harness.getPermanentId(player1, "Icehide Troll"));

        harness.castFromHand(player1, new AlrundsEpiphany(), "{5}{U}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Icehide Troll")).hasSize(3);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    void creatureTokensRemainTokensWhenCopyingACard() {
        Permanent target = addCreatureReady(player1, new IcehideTroll());
        castMysticReflection(target.getId());

        harness.castFromHand(player1, new AlrundsEpiphany(), "{5}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(target.getId())).toList())
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(gqs.isToken(gd, permanent)).isTrue());
    }

    @Test
    void aCreatureCardCopyingATokenRemainsNontoken() {
        harness.castFromHand(player1, new AlrundsEpiphany(), "{5}{U}{U}");
        harness.passBothPriorities();
        castMysticReflection(findPermanent(player1, "Bird").getId());

        IcehideTroll troll = new IcehideTroll();
        harness.castFromHand(player1, troll, "{2}{G}");
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(troll.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Bird");
        assertThat(gqs.isToken(gd, copy)).isFalse();
    }

    @Test
    void usesTargetsCopiableValuesAtEntryRatherThanResolution() {
        Permanent shapeshifter = addCreatureReady(player1, new Mistwalker());
        Permanent troll = addCreatureReady(player2, new IcehideTroll());
        castMysticReflection(shapeshifter.getId());

        harness.setHand(player1, List.of(new AbsorbIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, troll.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.castFromHand(player1, new IcehideTroll(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Icehide Troll")).hasSize(2);
        assertThat(findPermanents(player1, "Mistwalker")).isEmpty();
    }

    @Test
    void usesLastCopiableValuesWhenChangedTargetLeavesBeforeEntry() {
        Permanent shapeshifter = addCreatureReady(player1, new Mistwalker());
        Permanent troll = addCreatureReady(player2, new IcehideTroll());
        castMysticReflection(shapeshifter.getId());
        harness.setHand(player1, List.of(new AbsorbIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, troll.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new AbsorbIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, shapeshifter.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.castFromHand(player1, new IcehideTroll(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Icehide Troll")).hasSize(1);
        assertThat(findPermanents(player1, "Mistwalker")).isEmpty();
    }

    @Test
    void overlappingReflectionsRequireEnteringControllersChoice() {
        Permanent troll = addCreatureReady(player1, new IcehideTroll());
        Permanent walker = addCreatureReady(player1, new Mistwalker());
        castMysticReflection(troll.getId());
        castMysticReflection(walker.getId());

        harness.castFromHand(player1, new IcehideTroll(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void copiedCreatureEntersAbilityTriggers() {
        harness.addToBattlefield(player1, new RavenousLindwurm());
        castMysticReflection(harness.getPermanentId(player1, "Ravenous Lindwurm"));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new IcehideTroll(), "{2}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void copiedCreatureTokenEntersAbilityTriggers() {
        harness.addToBattlefield(player1, new RavenousLindwurm());
        castMysticReflection(harness.getPermanentId(player1, "Ravenous Lindwurm"));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new AlrundsEpiphany(), "{5}{U}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 8);
    }

    @Test
    void enteringCreaturesOriginalEntersAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new IcehideTroll());
        castMysticReflection(harness.getPermanentId(player1, "Icehide Troll"));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new RavenousLindwurm(), "{4}{G}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Icehide Troll")).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void planeswalkerEntersAsCreatureWithoutLoyaltyCounters() {
        harness.addToBattlefield(player1, new IcehideTroll());
        castMysticReflection(harness.getPermanentId(player1, "Icehide Troll"));
        harness.setHand(player1, List.of(new NikoAris()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent copy = findPermanents(player1, "Icehide Troll").getLast();
        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(copy.isSummoningSick()).isTrue();
        assertThat(findPermanents(player1, "Shard")).isEmpty();
    }

    @Test
    void cannotTargetALegendaryCreature() {
        Permanent legendary = addCreatureReady(player1, new MoritteOfTheFrost());
        legendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreaturePlaneswalker() {
        Permanent planeswalker = addCreatureReady(player1, new NikoAris());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replacementExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new IcehideTroll());
        castMysticReflection(harness.getPermanentId(player1, "Icehide Troll"));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Mistwalker(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Mistwalker")).hasSize(1);
    }

    @Test
    void foretoldReflectionCanBeCastOnOpponentsNextTurnForOneBlueMana() {
        harness.addToBattlefield(player1, new IcehideTroll());
        MysticReflection reflection = new MysticReflection();
        harness.setHand(player1, List.of(reflection));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, reflection.getId(),
                harness.getPermanentId(player1, "Icehide Troll"));
        harness.passBothPriorities();

        harness.castFromHand(player2, new Mistwalker(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Icehide Troll")).hasSize(1);
        harness.assertInGraveyard(player1, "Mystic Reflection");
    }

    @Test
    void cannotCastForetoldReflectionOnTheTurnItWasForetold() {
        harness.addToBattlefield(player1, new IcehideTroll());
        MysticReflection reflection = new MysticReflection();
        harness.setHand(player1, List.of(reflection));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, reflection.getId(),
                harness.getPermanentId(player1, "Icehide Troll")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetAtResolutionDoesNotCreateReplacement() {
        Permanent target = addCreatureReady(player1, new IcehideTroll());
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new AbsorbIdentity()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.castFromHand(player1, new Mistwalker(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mistwalker")).hasSize(1);
        harness.assertInGraveyard(player1, "Mystic Reflection");
    }

    @Test
    void targetCountersAndTappedStateAreNotCopied() {
        Permanent target = addCreatureReady(player1, new IcehideTroll());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();
        castMysticReflection(target.getId());

        harness.castFromHand(player1, new Mistwalker(), "{2}{U}");
        harness.passBothPriorities();

        Permanent copy = findPermanents(player1, "Icehide Troll").getLast();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(copy.isTapped()).isFalse();
    }

    private void castMysticReflection(UUID targetId) {
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Permanent castBears() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
