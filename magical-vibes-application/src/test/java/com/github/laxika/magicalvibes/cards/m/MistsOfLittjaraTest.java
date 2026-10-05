package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistsOfLittjara.class, GrizzlyBears.class, SleekSchooner.class, FountainOfYouth.class})
class MistsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castMistsOfLittjara(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mists of Littjara can enchant a noncreature Vehicle")
    void canEnchantVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());

        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, schooner.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mists of Littjara")
                        && schooner.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Mists of Littjara cannot enchant another noncreature permanent")
    void cannotEnchantOtherPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("The weakening ends when Mists of Littjara leaves the battlefield")
    void weakeningEndsWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castMistsOfLittjara(bears);

        Permanent aura = findPermanent(player1, "Mists of Littjara");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castMistsOfLittjara(bears);

        assertThat(findPermanent(player1, "Mists of Littjara").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Mists stack and affect only the enchanted creature")
    void multipleAurasStackOnOwnCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        castMistsOfLittjara(enchanted);
        castMistsOfLittjara(enchanted);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchanted Vehicle is weakened when crewed and keeps its Aura after cleanup")
    void vehicleIsWeakenedWhenCrewedAndRemainsEnchanted() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        addCreatureReady(player1, new GrizzlyBears());
        castMistsOfLittjara(schooner);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isFalse();
        harness.assertOnBattlefield(player1, "Mists of Littjara");
        assertThat(findPermanent(player1, "Mists of Littjara").getAttachedTo()).isEqualTo(schooner.getId());
    }

    private void castMistsOfLittjara(Permanent target) {
        harness.setHand(player1, List.of(new MistsOfLittjara()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
