package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarkhanSoulAflame.class, DragonWhelp.class, GrizzlyBears.class, Unsummon.class})
class SarkhanSoulAflameTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon spells you cast cost {1} less to cast")
    void reducesDragonSpellCost() {
        addSarkhan();
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Dragon creature spells do not get the cost reduction")
    void doesNotReduceNonDragonSpellCost() {
        addSarkhan();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Dragon entering under your control may be copied with Sarkhan's exceptions")
    void mayCopyEnteringDragon() {
        Permanent sarkhan = addSarkhan();

        castDragonWhelp();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sarkhan.getCard().getName()).isEqualTo("Sarkhan, Soul Aflame");
        assertThat(sarkhan.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Dragon entering under your control does not trigger the copy ability")
    void nonDragonDoesNotTriggerCopyAbility() {
        Permanent sarkhan = addSarkhan();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sarkhan.getCard().getName()).isEqualTo("Sarkhan, Soul Aflame");
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
    }

    @Test
    @DisplayName("The temporary Dragon copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent sarkhan = addSarkhan();
        castDragonWhelp();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sarkhan.getCard().getName()).isEqualTo("Sarkhan, Soul Aflame");
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
    }

    private Permanent addSarkhan() {
        return harness.addToBattlefieldAndReturn(player1, new SarkhanSoulAflame());
    }

    private void castDragonWhelp() {
        harness.castFromHand(player1, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void decliningCopyKeepsAbilityForNextDragon() {
        Permanent sarkhan = addSarkhan();
        castDragonWhelp();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);

        castDragonWhelp();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);
    }

    @Test
    void opponentsDragonDoesNotTriggerCopy() {
        Permanent sarkhan = addSarkhan();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
    }

    @Test
    void opponentsDragonDoesNotReceiveCostReduction() {
        addSarkhan();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DragonWhelp()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyingDragonRemovesCostReduction() {
        addSarkhan();
        castDragonWhelp();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyingDragonRemovesCopyTrigger() {
        addSarkhan();
        castDragonWhelp();
        harness.handleMayAbilityChosen(player1, true);

        harness.castFromHand(player1, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void copiedDragonActivatedAbilityWorks() {
        Permanent sarkhan = addSarkhan();
        castDragonWhelp();
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);
    }

    @Test
    void copiesDragonThatLeftBeforeResolution() {
        Permanent sarkhan = addSarkhan();
        harness.castFromHand(player1, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();
        var dragonId = harness.getPermanentId(player1, "Dragon Whelp");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, dragonId);
        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);
        assertThat(sarkhan.getCard().getName()).isEqualTo("Sarkhan, Soul Aflame");
        assertThat(sarkhan.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    void copyAbilityDoesNotTargetDragonWithShroud() {
        Permanent sarkhan = addSarkhan();
        harness.castFromHand(player1, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();
        Permanent dragon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Dragon Whelp"));
        dragon.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);
    }

    @Test
    void copyingDoesNotCopyCountersAndPreservesSarkhansCounters() {
        Permanent sarkhan = addSarkhan();
        sarkhan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castFromHand(player1, new DragonWhelp(), "{2}{R}{R}");
        harness.passBothPriorities();
        Permanent dragon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Dragon Whelp"));
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
        assertThat(sarkhan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reductionDoesNotReplaceRequiredColoredMana() {
        addSarkhan();
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
