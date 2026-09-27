package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainsOfCustody.class, GrizzlyBears.class, FountainOfYouth.class,
        Naturalize.class, Shock.class})
class ChainsOfCustodyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nonland permanent until the Aura leaves")
    void exilesPermanentUntilAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castAndResolve(creature.getId(), target.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("The exiled permanent returns when Chains of Custody leaves")
    void exiledPermanentReturnsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID auraId = harness.getPermanentId(player1, "Chains of Custody");

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("The enchanted creature has ward {2}")
    void enchantedCreatureHasWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Cannot exile a permanent controlled by the Aura's controller")
    void cannotTargetOwnPermanentForExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChainsOfCustody()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(creature.getId(), ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    private void castAndResolve(UUID creatureId, UUID permanentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChainsOfCustody()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, List.of(creatureId, permanentId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
