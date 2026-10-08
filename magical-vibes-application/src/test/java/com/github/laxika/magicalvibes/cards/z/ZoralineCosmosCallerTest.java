package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeatherOfFlight;
import com.github.laxika.magicalvibes.cards.r.RustShieldRampager;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoralineCosmosCaller.class, BarkformHarvester.class, Forest.class, RustShieldRampager.class, FeatherOfFlight.class})
class ZoralineCosmosCallerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB payment precedes target selection and a separately resolving return trigger")
    void etbReturnsPermanentWithFinalityCounter() {
        Card eligible = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(eligible, new Forest(), new RustShieldRampager()));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 18);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.assertInGraveyard(player1, "Barkform Harvester");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Barkform Harvester").getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Barkform Harvester");
    }

    @Test
    @DisplayName("Declining ETB payment requires no target and leaves graveyard and life unchanged")
    void decliningEtbPaymentDoesNothing() {
        Card eligible = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Barkform Harvester");
        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zoraline's own Bat attack gains 1 life")
    void batAttackGainsLife() {
        harness.setLife(player1, 10);
        addReadyZoraline(player1);

        declareAttackers(player1, List.of(0));
        resolveAttackTriggersDecliningPayment();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Attacking Zoraline pays before choosing a target for a separate return trigger")
    void attackReturnsPermanentWithFinalityCounter() {
        Card eligible = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(eligible));
        addReadyZoraline(player1);
        addReturnMana();

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.assertInGraveyard(player1, "Barkform Harvester");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Barkform Harvester").getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each other attacking Bat, including changelings, gains life separately")
    void multipleOtherBatAttacksGainLife() {
        addReadyZoraline(player1);
        addReadyCreature(player1, new BarkformHarvester());
        addReadyCreature(player1, new BarkformHarvester());
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.assertLife(player1, 11);
        harness.passBothPriorities();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("A non-Bat attacking does not gain life")
    void nonBatAttackDoesNotGainLife() {
        addReadyZoraline(player1);
        addReadyCreature(player1, new RustShieldRampager());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's Bat attack does not trigger Zoraline")
    void opponentBatAttackDoesNotGainLife() {
        addReadyZoraline(player1);
        addReadyCreature(player2, new BarkformHarvester());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB payment can be made without any qualifying graveyard target")
    void canPayWithoutEligibleTarget() {
        harness.setGraveyard(player1, List.of(new Forest(), new RustShieldRampager()));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Rust-Shield Rampager");
        harness.assertNotOnBattlefield(player1, "Rust-Shield Rampager");
    }

    @Test
    @DisplayName("Insufficient life prevents paying either part of the return cost")
    void insufficientLifeDoesNotSpendMana() {
        harness.setLife(player1, 1);
        harness.setGraveyard(player1, List.of(new BarkformHarvester()));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        harness.assertInGraveyard(player1, "Barkform Harvester");
        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("A returned Aura enters attached to a chosen legal creature with a finality counter")
    void returnedAuraEntersAttached() {
        Card aura = new FeatherOfFlight();
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        harness.setLibrary(player1, List.of(new Forest()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, host.getId());

        Permanent returned = findPermanent(player1, "Feather of Flight");
        assertThat(returned.getAttachedTo()).isEqualTo(host.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Feather of Flight");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A permanent returned with a finality counter is exiled instead of dying")
    void returnedPermanentIsExiledInsteadOfDying() {
        Card eligible = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        findPermanent(player1, "Barkform Harvester").setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        harness.assertNotInGraveyard(player1, "Barkform Harvester");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(eligible.getId()));
    }

    private void addZoralineMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addReadyZoraline(Player player) {
        addReadyCreature(player, new ZoralineCosmosCaller());
    }

    private void addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
    }

    private void resolveAttackTriggersDecliningPayment() {
        for (int i = 0; i < 4 && (!gd.stack.isEmpty() || !gd.pendingMayAbilities.isEmpty()); i++) {
            if (!gd.pendingMayAbilities.isEmpty()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
