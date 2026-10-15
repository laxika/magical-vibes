package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraHopesBeacon.class, GrizzlyBears.class, LightningBolt.class, Opt.class, BonecrusherGiant.class})
class ChandraHopesBeaconTest extends BaseCardTest {

    @Test
    @DisplayName("The copy ability triggers only for the first instant or sorcery each turn")
    void copiesOnlyFirstInstantOrSorceryEachTurn() {
        addReadyChandra(6);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    @DisplayName("+2 adds two mana with independently chosen colors")
    void plusTwoAddsTwoManaInAnyCombination() {
        Permanent chandra = addReadyChandra(5);
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
    }

    @Test
    @DisplayName("+1 exiles five cards and grants next-turn cast permission only to instants and sorceries")
    void plusOneExilesFiveAndGrantsFilteredPermission() {
        Permanent chandra = addReadyChandra(5);
        LightningBolt bolt = new LightningBolt();
        Opt opt = new Opt();
        GrizzlyBears bears = new GrizzlyBears();
        LightningBolt secondBolt = new LightningBolt();
        Opt secondOpt = new Opt();
        harness.setLibrary(player1, List.of(bolt, bears, opt, secondBolt, secondOpt));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(bolt.getId(), bears.getId(), opt.getId(), secondBolt.getId(), secondOpt.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(bolt.getId(), player1.getId())
                .containsEntry(opt.getId(), player1.getId())
                .containsEntry(secondBolt.getId(), player1.getId())
                .containsEntry(secondOpt.getId(), player1.getId())
                .doesNotContainKey(bears.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, bolt.getId(), player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(bolt.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bolt);
    }

    @Test
    @DisplayName("-X deals X damage to each of up to two targets")
    void minusXDamagesEachTarget() {
        Permanent chandra = addReadyChandra(6);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, 3, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void plusOneAllowsOnlyOneSpellFromItsExiledCards() {
        addReadyChandra(5);
        LightningBolt first = new LightningBolt();
        LightningBolt second = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
    }

    @Test
    void plusOneGrantsPermissionForAnExiledAdventureCard() {
        addReadyChandra(5);
        BonecrusherGiant giant = new BonecrusherGiant();
        harness.setLibrary(player1, List.of(giant));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(giant);
        assertThat(gd.exilePlayPermissions).containsEntry(giant.getId(), player1.getId());
    }

    @Test
    void copyResolvesBeforeOriginalAndDoesNotTriggerAnotherCopy() {
        addReadyChandra(5);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureCastDoesNotConsumeTheOncePerTurnTrigger() {
        addReadyChandra(5);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    void minusXCanUseAllLoyaltyAndStillDealDamage() {
        addReadyChandra(5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, 5, List.of(player2.getId()));
        harness.assertInGraveyard(player1, "Chandra, Hope's Beacon");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    void minusXAllowsZeroAndNoTargets() {
        Permanent chandra = addReadyChandra(5);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, 0, List.of());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyChandra(int loyalty) {
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraHopesBeacon());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }
}
