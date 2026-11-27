package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TippyToeTerrificPartner;
import com.github.laxika.magicalvibes.cards.p.PolisCrusher;
import com.github.laxika.magicalvibes.cards.r.RoyalTreatment;
import com.github.laxika.magicalvibes.cards.t.TerritorialWitchstalker;
import com.github.laxika.magicalvibes.cards.t.ToadstoolAdmirer;
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

@CardUsed({AsinineAntics.class, GrizzlyBears.class, PolisCrusher.class,
        RoyalTreatment.class, TerritorialWitchstalker.class, ToadstoolAdmirer.class, AnointedProcession.class,
        TippyToeTerrificPartner.class})
class AsinineAnticsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Cursed Role attached to each creature an opponent controls")
    void createsCursedRoleAttachedToEachOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNormally();

        List<Permanent> roles = findPermanents(player1, "Cursed");
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstOpponentCreature.getId(), secondOpponentCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates no Role for a creature it couldn't enchant")
    void doesNotCreateRoleThatCannotEnchantAnOpposingCreature() {
        harness.addToBattlefieldAndReturn(player2, new PolisCrusher());
        Permanent legalCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(legalCreature.getId());
    }

    @Test
    @DisplayName("Can be cast at instant speed for two more mana")
    void canBeCastAtInstantSpeedForTwoMoreMana() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AsinineAntics()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, List.of());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(opponentCreature.getId());
    }

    @Test
    @DisplayName("Resolves without effect when opponents control no creatures")
    void resolvesWithoutOpposingCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TerritorialWitchstalker());

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A second casting replaces its caster's older Role")
    void secondCastingReplacesTheCastersOlderRole() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TerritorialWitchstalker());
        castNormally();
        Permanent oldRole = findPermanents(player1, "Cursed").getFirst();

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).singleElement().satisfies(role -> {
            assertThat(role.getId()).isNotEqualTo(oldRole.getId());
            assertThat(role.getAttachedTo()).isEqualTo(creature.getId());
        });
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchants a hexproof creature and leaves an opponent's Role on it")
    void enchantsHexproofCreatureWithoutRemovingOpponentsRoleOrItsBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TerritorialWitchstalker());
        harness.setHand(player2, List.of(new RoyalTreatment()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        Permanent royalRole = findPermanents(player2, "Royal").getFirst();

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo).isEqualTo(creature.getId());
        assertThat(findPermanents(player2, "Royal")).containsExactly(royalRole);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Doesn't trigger ward and keeps +1/+1 counters counting")
    void doesNotTriggerWardAndPreservesPlusOneCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ToadstoolAdmirer());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The normal cost doesn't allow casting on an opponent's turn")
    void normalCostDoesNotPermitCastingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AsinineAntics()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("After the creating player orders a doubler and an addition, each doubled Role is attached to its creature")
    void doubledRolesAreAttachedAfterTheReplacementOrder() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNormally();
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.startsWith("Anointed Procession")).findFirst().orElseThrow());

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Cursed")).isNotEmpty()
                .allSatisfy(role -> assertThat(role.getAttachedTo()).isEqualTo(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    private void castNormally() {
        harness.castFromHand(player1, new AsinineAntics(), "{2}{U}{U}");
        harness.passBothPriorities();
    }
}
