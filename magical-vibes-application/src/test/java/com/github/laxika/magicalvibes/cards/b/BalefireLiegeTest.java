package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalefireLiege.class, HillGiant.class, SuntailHawk.class, GrizzlyBears.class, ChandraNalaar.class})
class BalefireLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOwnRedCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-red non-white creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a red spell deals 3 damage to the chosen player")
    void redSpellDealsDamage() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
    }

    @Test
    @DisplayName("Casting a white spell does not fire the red damage trigger")
    void whiteSpellDoesNotDealDamage() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Casting a white spell gains 3 life")
    void whiteSpellGainsLife() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int p1LifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 3);
    }

    @Test
    @DisplayName("Casting a red spell does not gain life")
    void redSpellGainsNoLife() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        int p1LifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore);
    }

    @Test
    @DisplayName("A single Liege does not boost itself")
    void doesNotBoostItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new BalefireLiege());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both bonuses apply to another red and white creature")
    void bothBonusesApplyToAnotherLiege() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalefireLiege());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalefireLiege());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opposing red and white creatures receive neither bonus")
    void doesNotBoostOpposingCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        Permanent opponentLiege = harness.addToBattlefieldAndReturn(player2, new BalefireLiege());

        assertThat(gqs.getEffectivePower(gd, opponentLiege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentLiege)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a hybrid red-white spell triggers both abilities even with only red mana")
    void hybridSpellTriggersBothAbilities() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new BalefireLiege()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(countPermanents(player1, "Balefire Liege")).isEqualTo(2);
    }

    @Test
    @DisplayName("The Liege being cast does not trigger its own abilities")
    void doesNotTriggerFromItsOwnCast() {
        harness.setHand(player1, List.of(new BalefireLiege()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Balefire Liege")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's red-white spell triggers neither ability")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player2, List.of(new BalefireLiege()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player2, "Balefire Liege")).isEqualTo(1);
    }

    @Test
    @DisplayName("A green spell triggers neither ability")
    void offColorSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The damage trigger may target its controller")
    void redTriggerCanTargetController() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The red trigger damages a planeswalker without damaging its controller")
    void redTriggerCanTargetPlaneswalker() {
        harness.addToBattlefield(player1, new BalefireLiege());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, chandra.getId());
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both cast triggers resolve after the source leaves the battlefield")
    void triggersSurviveSourceLeavingBattlefield() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new BalefireLiege()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(liege);
        harness.setGraveyard(player1, List.of(liege.getCard()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
