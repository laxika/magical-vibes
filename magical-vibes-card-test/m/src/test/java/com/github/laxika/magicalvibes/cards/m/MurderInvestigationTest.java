package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurderInvestigation.class, GiantSpider.class, GrizzlyBears.class, FountainOfYouth.class})
class MurderInvestigationTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, creates Soldiers equal to its power")
    void createsSoldiersEqualToDyingCreaturePower() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent investigation = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        investigation.setAttachedTo(spider.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(countSoldiers(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant an opponent's creature")
    void cannotEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MurderInvestigation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MurderInvestigation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void canEnchantOwnCreatureAndTriggerAfterAuraGoesToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MurderInvestigation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Murder Investigation").getAttachedTo()).isEqualTo(creature.getId());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Murder Investigation");
        harness.passBothPriorities();

        assertThat(countSoldiers(player1)).isEqualTo(2);
        assertThat(countSoldiers(player2)).isZero();
        assertThat(findPermanents(player1, "Soldier")).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    void usesModifiedPowerAsCreatureLastExistedOnBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        creature.setPowerModifier(3);
        creature.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(countSoldiers(player1)).isEqualTo(5);
    }

    @Test
    void negativePowerCreatesNoTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        creature.setPowerModifier(-3);
        creature.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(countSoldiers(player1)).isZero();
    }

    @Test
    void exileDoesNotCreateTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Murder Investigation");
        assertThat(countSoldiers(player1)).isZero();
    }

    @Test
    void changingCreatureControllerMakesAuraIllegalWithoutTriggering() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Murder Investigation");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countSoldiers(player1)).isZero();
        assertThat(countSoldiers(player2)).isZero();
    }

    @Test
    @CardUsed(Hushbringer.class)
    void hushbringerSuppressesEnchantedCreatureDeathTrigger() {
        harness.addToBattlefield(player2, new Hushbringer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countSoldiers(player1)).isZero();
    }

    @Test
    void auraWithoutAbilitiesDoesNotTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        aura.setLosesAllAbilitiesUntilEndOfTurn(true);
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(countSoldiers(player1)).isZero();
    }

    @Test
    void simultaneousDestructionOfAuraAndCreatureStillCreatesTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MurderInvestigation());
        aura.setAttachedTo(creature.getId());
        var removal = harness.getPermanentRemovalService();
        harness.inMutationScope(() -> removal.performSimultaneousRemovals(gd, List.of(aura, creature), () -> {
            removal.destroyPermanentToGraveyard(gd, aura);
            removal.destroyPermanentToGraveyard(gd, creature);
        }));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Murder Investigation");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countSoldiers(player1)).isEqualTo(2);
    }

    private long countSoldiers(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .count();
    }
}
