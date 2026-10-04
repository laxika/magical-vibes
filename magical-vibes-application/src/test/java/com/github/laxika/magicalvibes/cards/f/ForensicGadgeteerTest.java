package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoustingDummy;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForensicGadgeteer.class, MagnifyingGlass.class, GrizzlyBears.class, JoustingDummy.class,
        TezzeretBetrayerOfFlesh.class})
class ForensicGadgeteerTest extends BaseCardTest {

    @Test
    void stackedReductionsKeepOneManaMinimum() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 3, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void investigatesWhenControllerCastsArtifactSpell() {
        addCreatureReady(player1, new ForensicGadgeteer());
        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenControllerCastsNonartifactSpell() {
        addCreatureReady(player1, new ForensicGadgeteer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void reducesActivatedAbilitiesOfArtifactsYouControl() {
        addCreatureReady(player1, new ForensicGadgeteer());
        Permanent glass = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(glass), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceActivatedAbilitiesOfArtifactsAnOpponentControls() {
        addCreatureReady(player1, new ForensicGadgeteer());
        Permanent glass = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(glass), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void investigatesBeforeArtifactSpellResolves() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Magnifying Glass");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenOpponentCastsArtifactSpell() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MagnifyingGlass(), "{3}");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertOnBattlefield(player2, "Magnifying Glass");
    }

    @Test
    void investigatesForEachArtifactSpellAndEachGadgeteer() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new ForensicGadgeteer());

        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");
        resolveAllTriggers();
        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(4);
    }

    @Test
    void creatingArtifactTokensDoesNotTriggerAnotherInvestigation() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void clueCostsOneManaAndStillRequiresSacrificeToDraw() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.setLibrary(player1, List.of(new ForensicGadgeteer()));
        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void stackedReductionsDoNotAllowActivationWithoutMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ForensicGadgeteer());
        }
        harness.addToBattlefield(player1, new MagnifyingGlass());

        assertThatThrownBy(() -> harness.activateAbility(player1, 4, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void manaAbilityWithNoManaCostRemainsFree() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new MagnifyingGlass());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @CardUsed({ForensicGadgeteer.class, MagnifyingGlass.class, TezzeretBetrayerOfFlesh.class})
    void unrestrictedReductionCanMakeClueActivationFreeDespiteGadgeteer() {
        harness.addToBattlefield(player1, new ForensicGadgeteer());
        harness.addToBattlefield(player1, new TezzeretBetrayerOfFlesh());
        harness.setLibrary(player1, List.of(new ForensicGadgeteer()));
        harness.castFromHand(player1, new MagnifyingGlass(), "{3}");
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        int handSize = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
