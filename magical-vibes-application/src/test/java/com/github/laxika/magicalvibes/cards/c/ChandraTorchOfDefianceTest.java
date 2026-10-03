package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ChandraTorchOfDefiance.class, GrizzlyBears.class, Plains.class, Shock.class, PaladinEnVec.class})
class ChandraTorchOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("First +1 deals 2 damage when the exiled card is a land")
    void firstPlusOneDealsDamageForLand() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card land = new Plains();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("First +1 casts a nonland exiled card for its normal cost")
    void firstPlusOneCastsExiledCard() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("First +1 keeps an uncastable nonland card in exile and deals damage")
    void firstPlusOneKeepsUncastableCardExiled() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Second +1 adds two red mana")
    void secondPlusOneAddsTwoRedMana() {
        Permanent chandra = addReadyChandra(player1, 4);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore + 2);
    }

    @Test
    @DisplayName("Minus three deals four damage to a target creature")
    void minusThreeDamagesCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Minus seven creates an emblem that deals five damage on a spell cast")
    void minusSevenCreatesSpellCastDamageEmblem() {
        addReadyChandra(player1, 7);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("First +1 still deals damage with an empty library")
    void firstPlusOneDealsDamageWithEmptyLibrary() {
        addReadyChandra(player1, 4);
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Declining an affordable exiled spell deals damage without spending mana")
    void firstPlusOneDeclinesAffordableSpell() {
        addReadyChandra(player1, 4);
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("First +1 casts a creature during resolution and does not deal damage")
    void firstPlusOneCastsCreatureDuringResolution() {
        addReadyChandra(player1, 4);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Mana-producing +1 uses the stack and cannot be activated twice in a turn")
    void secondPlusOneUsesStackAndLoyaltyLimit() {
        addReadyChandra(player1, 4);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Minus three cannot target a player")
    void minusThreeRejectsPlayerTarget() {
        Permanent chandra = addReadyChandra(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Emblem damage is colorless and kills a creature with protection from red")
    void emblemDamageIgnoresProtectionFromRed() {
        addReadyChandra(player1, 7);
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, paladin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Paladin en-Vec");
        harness.assertInGraveyard(player2, "Paladin en-Vec");
    }

    @Test
    @DisplayName("Opponent spells do not trigger the emblem")
    void emblemDoesNotTriggerForOpponentSpell() {
        addReadyChandra(player1, 7);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraTorchOfDefiance());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
