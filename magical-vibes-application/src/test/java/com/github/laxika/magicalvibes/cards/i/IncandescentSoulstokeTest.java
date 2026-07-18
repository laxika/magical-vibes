package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncandescentSoulstokeTest extends BaseCardTest {

    // ===== Static anthem — "Other Elemental creatures you control get +1/+1" =====

    @Test
    @DisplayName("Boosts other Elemental creatures you control by +1/+1")
    void boostsOtherElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent elemental = findPermanent(player1, "Air Elemental");

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());

        Permanent soulstoke = findPermanent(player1, "Incandescent Soulstoke");

        assertThat(gqs.getEffectivePower(gd, soulstoke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soulstoke)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-Elemental creatures")
    void doesNotBoostNonElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's Elemental creatures")
    void doesNotBoostOpponentElementals() {
        harness.addToBattlefield(player1, new IncandescentSoulstoke());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent opponentElemental = findPermanent(player2, "Air Elemental");

        assertThat(gqs.getEffectivePower(gd, opponentElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentElemental)).isEqualTo(4);
    }

    // ===== Activated ability — Sneak-Attack for Elementals =====

    @Test
    @DisplayName("Ability offers only Elemental creature cards in hand")
    void abilityOffersOnlyElementalCreatures() {
        addReadySoulstoke();
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears(), new AirElemental()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    @Test
    @DisplayName("Chosen Elemental enters with haste and is scheduled for end-step sacrifice")
    void chosenElementalEntersWithHasteAndEndStepSacrifice() {
        addReadySoulstoke();
        harness.setHand(player1, List.of(new AirElemental()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(elemental.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Declining the may leaves the Elemental in hand")
    void decliningLeavesElementalInHand() {
        addReadySoulstoke();
        harness.setHand(player1, List.of(new AirElemental()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    private Permanent addReadySoulstoke() {
        Permanent soulstoke = new Permanent(new IncandescentSoulstoke());
        soulstoke.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(soulstoke);
        return soulstoke;
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
