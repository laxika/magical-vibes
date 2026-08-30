package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({SarkhanSoulAflame.class, DragonWhelp.class, GrizzlyBears.class})
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
