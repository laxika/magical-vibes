package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BalothPup;
import com.github.laxika.magicalvibes.cards.n.NissasJudgment;
import com.github.laxika.magicalvibes.cards.s.SpatialContortion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirrorpool.class, AngelsMercy.class, GrizzlyBears.class, BalothPup.class,
        NissasJudgment.class, SpatialContortion.class})
class MirrorpoolTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and adds one colorless mana")
    void entersTappedAndAddsColorlessMana() {
        harness.setHand(player1, List.of(new Mirrorpool()));

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Copies an instant or sorcery spell you control")
    void copiesOwnInstantOrSorcerySpell() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, 1, null, mercy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirrorpool);
        harness.assertInGraveyard(player1, "Mirrorpool");
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(34);
    }

    @Test
    @DisplayName("Creates a token copy of a creature you control")
    void createsTokenCopyOfOwnCreature() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirrorpool);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature or a creature spell")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);

        GrizzlyBears bearsSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(bearsSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void cannotPayColorlessRequirementWithColoredMana() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, pup.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mirrorpool, pup);
        assertThat(mirrorpool.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateCopyAbilityWhileTapped() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        mirrorpool.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, pup.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mirrorpool, pup);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCopyOpponentsInstant() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        SpatialContortion spell = new SpatialContortion();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, pup.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mirrorpool");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canChooseNewTargetForCopiedInstant() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        SpatialContortion spell = new SpatialContortion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, first.getId());
        harness.activateAbility(player1, 0, 1, null, spell.getId());

        harness.assertInGraveyard(player1, "Mirrorpool");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayKeepOriginalTargetForCopiedInstant() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        SpatialContortion spell = new SpatialContortion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, pup.getId());
        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(pup);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void createsNoTokenWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setHand(player1, List.of(new SpatialContortion()));

        harness.activateAbility(player1, 0, 2, null, pup.getId());
        harness.assertInGraveyard(player1, "Mirrorpool");
        harness.castAndResolveInstant(player1, 0, pup.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Baloth Pup");
    }

    @Test
    void tokenDoesNotCopyCountersOrTappedStatus() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        pup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        pup.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, pup.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.isSummoningSick()).isTrue();
                    assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isFalse();
                });
        assertThat(gqs.hasKeyword(gd, pup, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canChooseNewTargetsForEveryTargetOfCopiedSorcery() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent firstNew = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent secondNew = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        Permanent newDamageTarget = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        NissasJudgment spell = new NissasJudgment();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), damageTarget.getId()));
        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstNew.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondNew.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, newDamageTarget.getId());
        harness.passBothPriorities();

        assertThat(firstNew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondNew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damageTarget).doesNotContain(newDamageTarget);

        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damageTarget, newDamageTarget);
        assertThat(gd.stack).isEmpty();
    }

}
