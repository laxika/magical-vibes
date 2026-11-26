package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.b.BasaltGargoyle;
import com.github.laxika.magicalvibes.cards.d.DeepcavernImp;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.s.SubterraneanShambler;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ThickSkinnedGoblin.class, BasaltGargoyle.class, SuddenShock.class,
        SuddenSpoiling.class, SubterraneanShambler.class, DeepcavernImp.class, FomoriNomad.class})
class ThickSkinnedGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Echo of a permanent you control may be paid with {0}")
    void mayPayZeroForEcho() {
        harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Basalt Gargoyle");
    }

    @Test
    @DisplayName("Declining to pay echo sacrifices the permanent")
    void mayDeclineEchoAndSacrifice() {
        harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Thick-Skinned Goblin");
        harness.assertInGraveyard(player1, "Basalt Gargoyle");
    }

    @Test
    @DisplayName("{R}: gains protection from red until end of turn")
    void activatedAbilityGrantsProtectionUntilEndOfTurn() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.protectionColorsOf(gd, goblin)).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.protectionColorsOf(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Protection from red stops a red spell from targeting it")
    void protectionFromRedPreventsRedSpellTargeting() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing its abilities before echo resolves takes away the {0} alternative")
    void losingAbilitiesRemovesAlternativeForPendingEcho() {
        harness.addToBattlefield(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        resolveAllTriggers();
        giveManaSource(player1);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new SuddenSpoiling()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Thick-Skinned Goblin");
        harness.assertNotOnBattlefield(player1, "Basalt Gargoyle");
        harness.assertInGraveyard(player1, "Basalt Gargoyle");
    }

    @Test
    @DisplayName("The {0} alternative is gone once the Goblin left before echo resolves")
    void removingGoblinBeforeEchoResolvesRemovesAlternative() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        resolveAllTriggers();
        giveManaSource(player1);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, goblin.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Thick-Skinned Goblin");
        harness.assertNotOnBattlefield(player1, "Basalt Gargoyle");
        harness.assertInGraveyard(player1, "Basalt Gargoyle");
    }

    @Test
    @DisplayName("An opponent's Goblin gives no {0} alternative")
    void opponentsGoblinDoesNotProvideAlternativeEchoCost() {
        harness.addToBattlefield(player2, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        resolveAllTriggers();
        giveManaSource(player1);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Thick-Skinned Goblin");
        harness.assertNotOnBattlefield(player1, "Basalt Gargoyle");
        harness.assertInGraveyard(player1, "Basalt Gargoyle");
    }

    @Test
    @DisplayName("Protection from red prevents untargeted red damage")
    void protectionPreventsUntargetedRedDamage() {
        harness.addToBattlefield(player1, new ThickSkinnedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.castFromHand(player1, new SubterraneanShambler(), "{3}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Thick-Skinned Goblin");
        harness.assertNotInGraveyard(player1, "Thick-Skinned Goblin");
    }

    @Test
    @DisplayName("Declining the {0} alternative, the payer may still pay the echo cost itself: a card is discarded and "
            + "the permanent stays (its ruling)")
    void mayPayThePrintedEchoCostInsteadOfZero() {
        harness.addToBattlefield(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new DeepcavernImp(), "{2}{B}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new FomoriNomad()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .endsWith("Pay {0}?");
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .isEqualTo("Discard a card?");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Deepcavern Imp");
        harness.assertInGraveyard(player1, "Fomori Nomad");
    }
}
