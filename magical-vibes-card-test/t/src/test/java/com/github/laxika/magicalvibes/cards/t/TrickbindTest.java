package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IsolationCell;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.k.KarnSilverGolem;
import com.github.laxika.magicalvibes.cards.p.ParadisePlume;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trickbind.class, RodOfRuin.class, IsolationCell.class, GrizzlyBears.class, Shock.class,
        PrismaticLens.class, TerramorphicExpanse.class, ParadisePlume.class, AshcoatBear.class,
        KarnSilverGolem.class, HermeticStudy.class})
class TrickbindTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and prevents the source from activating again")
    void countersActivatedAbilityAndLocksSource() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        findPermanent(player2, "Rod of Ruin").untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The source lock expires at end of turn")
    void sourceLockExpiresAtEndOfTurn() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        findPermanent(player2, "Rod of Ruin").untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Counters a triggered ability")
    void countersTriggeredAbility() {
        harness.addToBattlefield(player1, new IsolationCell());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Cannot target a spell")
    void cannotTargetSpell() {
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Split second prevents a spell response")
    void splitSecondPreventsSpellResponse() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, rod.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Split second permits mana abilities while Trickbind is on the stack")
    void splitSecondPermitsManaAbilities() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        var lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());

        harness.activateAbility(player2, 1, 0, null, null);

        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Split second prevents another permanent's nonmana activation")
    void splitSecondPreventsNonManaActivation() {
        harness.addToBattlefield(player2, new RodOfRuin());
        var otherRod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(otherRod.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Only the targeted ability is countered when a source has multiple abilities on the stack")
    void leavesPreviouslyActivatedAbilityOnStack() {
        var rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 9);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        var firstAbilityId = gd.stack.getLast().getTargetableId();
        rod.untap();
        harness.activateAbility(player2, 0, null, player1.getId());
        var secondAbilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);
        harness.castInstant(player1, 0, secondAbilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(firstAbilityId);
        rod.untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Counters an ability even after its source was sacrificed to pay its cost")
    void countersAbilityOfSacrificedSource() {
        harness.addToBattlefield(player2, new TerramorphicExpanse());
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.assertNotOnBattlefield(player2, "Terramorphic Expanse");
        harness.assertInGraveyard(player2, "Terramorphic Expanse");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Trickbind");
    }

    @Test
    @DisplayName("Countering a permanent's trigger locks its mana ability but allows future triggers")
    void counteredTriggerLocksManaAbilityWithoutSuppressingTriggers() {
        var plume = harness.addToBattlefieldAndReturn(player2, new ParadisePlume());
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plume.isTapped()).isFalse();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Countering Isolation Cell's trigger locks an activated ability granted to that permanent")
    void counteredIsolationCellTriggerLocksGrantedAbility() {
        var cell = harness.addToBattlefieldAndReturn(player1, new IsolationCell());
        cell.setSummoningSick(false);
        harness.addToBattlefield(player1, new KarnSilverGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, cell.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new HermeticStudy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, cell.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        cell.untap();

        harness.setHand(player1, List.of(new Trickbind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cell.isTapped()).isFalse();
    }
}
