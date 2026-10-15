package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BackupAgent;
import com.github.laxika.magicalvibes.cards.c.CementShoes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaffinesGuidance.class, BackupAgent.class, CementShoes.class})
class RaffinesGuidanceTest extends BaseCardTest {

    @Test
    void enchantsCreatureWithPlusOnePlusOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        harness.setHand(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canBeCastFromGraveyardForTwoAndWhite() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        harness.setGraveyard(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Raffine's Guidance");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new CementShoes());
        harness.setHand(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void cannotUsePrintedManaCostFromGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        harness.setGraveyard(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay graveyard cast cost");

        harness.assertInGraveyard(player1, "Raffine's Guidance");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void graveyardCastingStillRequiresMainPhase() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        harness.setGraveyard(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");

        harness.assertInGraveyard(player1, "Raffine's Guidance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetReturnsGraveyardCastAuraToGraveyardAndAllowsAnotherCast() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        harness.setGraveyard(player1, List.of(new RaffinesGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raffine's Guidance");
        harness.assertNotOnBattlefield(player1, "Raffine's Guidance");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFlashback(player1, 0, otherCreature.getId());

        harness.assertOnBattlefield(player1, "Raffine's Guidance");
        harness.assertNotInGraveyard(player1, "Raffine's Guidance");
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }
}
