package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.l.LanternBearer;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorinImperiousBloodlord.class, CaptivatingVampire.class, LanternBearer.class,
        ImprisonedInTheMoon.class, SigardaHostOfHerons.class})
class SorinImperiousBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 4 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new SorinImperiousBloodlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent sorin = findPermanent(player1, "Sorin, Imperious Bloodlord");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }


    @Test
    @DisplayName("+1 grants deathtouch and lifelink to a controlled creature")
    void plusOneGrantsKeywords() {
        Permanent sorin = addReadySorin(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LanternBearer());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("+1 puts a +1/+1 counter when the target is a Vampire")
    void plusOnePutsCounterOnVampire() {
        Permanent sorin = addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());

        harness.activateAbility(player1, 0, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isTrue();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 cannot target an opponent's creature")
    void plusOneRejectsOpponentCreature() {
        addReadySorin(player1);
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LanternBearer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("+1 sacrifice Vampire deals 3 damage and gains 3 life")
    void plusOneSacrificeDealsDamageAndGainsLife() {
        Permanent sorin = addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        int lifeBefore = gd.getLife(player1.getId());
        int oppLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Captivating Vampire");
        assertThat(gd.getLife(player2.getId())).isEqualTo(oppLifeBefore - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("+1 declining sacrifice deals no damage and gains no life")
    void plusOneDeclineSacrificeDoesNothing() {
        addReadySorin(player1);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        int lifeBefore = gd.getLife(player1.getId());
        int oppLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(oppLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Captivating Vampire");
    }

    @Test
    @DisplayName("+1 sacrifice only offers Vampires")
    void plusOneSacrificeOnlyOffersVampires() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new LanternBearer());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(vampire.getId());
    }

    @Test
    @DisplayName("+1 sacrifice rider offers a planeswalker, but not one Imprisoned in the Moon turned into a land")
    void plusOneSacrificeRiderReadsTheDeclaredAnyTarget() {
        Permanent sorin = addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent opponentSorin = harness.addToBattlefieldAndReturn(player2, new SorinImperiousBloodlord());
        opponentSorin.setCounterCount(CounterType.LOYALTY, 3);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(opponentSorin.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds())
                .contains(sorin.getId(), player1.getId(), player2.getId())
                .doesNotContain(opponentSorin.getId(), aura.getId());
    }


    @Test
    @DisplayName("−3 puts a Vampire creature from hand onto the battlefield")
    void minusThreePutsVampireFromHand() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Captivating Vampire");
    }

    @Test
    @DisplayName("−3 declining puts nothing from hand")
    void minusThreeDeclinePutsNothing() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("−3 only offers Vampire creature cards")
    void minusThreeOnlyOffersVampireCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire(), new LanternBearer()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Granted keywords expire at cleanup but the Vampire counter remains")
    void keywordsExpireButCounterRemains() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());

        harness.activateAbility(player1, 0, 0, null, vampire.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isFalse();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice trigger excludes opposing hexproof creatures but allows your own")
    void sacrificeTriggerRespectsHexproof() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent ownSigarda = harness.addToBattlefieldAndReturn(player1, new SigardaHostOfHerons());
        Permanent opposingSigarda = harness.addToBattlefieldAndReturn(player2, new SigardaHostOfHerons());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(ownSigarda.getId()).doesNotContain(opposingSigarda.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingSigarda.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice trigger damages a creature only after its separate resolution")
    void sacrificeTriggerDamagesCreature() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LanternBearer());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInGraveyard(player1, "Captivating Vampire");
        harness.assertOnBattlefield(player2, "Lantern Bearer");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lantern Bearer");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Sacrifice trigger does not gain life if its target becomes a land")
    void sacrificeTriggerIllegalTargetPreventsLifeGain() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LanternBearer());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lantern Bearer");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Sacrifice trigger removes three loyalty from a planeswalker")
    void sacrificeTriggerDamagesPlaneswalker() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SorinImperiousBloodlord());
        target.setCounterCount(CounterType.LOYALTY, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Minus three still puts a Vampire onto the battlefield when Sorin dies at zero loyalty")
    void minusThreeResolvesAfterSorinDies() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Sorin, Imperious Bloodlord");
        harness.assertOnBattlefield(player1, "Captivating Vampire");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadySorin(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SorinImperiousBloodlord());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
