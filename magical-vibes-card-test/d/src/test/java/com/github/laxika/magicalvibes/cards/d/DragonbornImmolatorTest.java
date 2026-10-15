package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonbornImmolator.class, DoomBlade.class, GrizzlyBears.class, GloriousAnthem.class})
class DragonbornImmolatorTest extends BaseCardTest {

    @Test
    void deathPowerBecomesTheNextCreatureSpellsPerpetualBoost() {
        Permanent immolator = addCreatureReady(player1, new DragonbornImmolator());
        killWithDoomBlade(immolator);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, firstBears))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, secondBears))).isEqualTo(2);
    }

    @Test
    void zeroPowerDoesNotCreateTheBoon() {
        Permanent immolator = addCreatureReady(player1, new DragonbornImmolator());
        immolator.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        killWithDoomBlade(immolator);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, bears))).isEqualTo(2);
    }

    @Test
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent immolator = addCreatureReady(player1, new DragonbornImmolator());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(2);
    }

    private void killWithDoomBlade(Permanent creature) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();
    }

    @Test
    void positiveLastKnownPowerIncludesControllerDependentStaticBonuses() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent immolator = addCreatureReady(player1, new DragonbornImmolator());
        immolator.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(1);
        killWithDoomBlade(immolator);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, bears))).isEqualTo(4);
    }

    @Test
    void deathNotesPowerAfterRepeatedActivations() {
        Permanent immolator = addCreatureReady(player1, new DragonbornImmolator());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        killWithDoomBlade(immolator);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent boostedBears = findPermanentByCard(player1, bears);
        assertThat(gqs.getEffectivePower(gd, boostedBears)).isEqualTo(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, boostedBears)).isEqualTo(6);
        killWithDoomBlade(boostedBears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(bears));
        gd.playerGraveyards.get(player1.getId()).remove(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, bears))).isEqualTo(6);
    }

    @Test
    void multipleBoonsApplyToTheSameNextCreatureSpell() {
        Permanent first = addCreatureReady(player1, new DragonbornImmolator());
        Permanent second = addCreatureReady(player1, new DragonbornImmolator());
        killWithDoomBlade(first);
        killWithDoomBlade(second);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, firstBears))).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, secondBears))).isEqualTo(2);
    }

    private Permanent findPermanentByCard(com.github.laxika.magicalvibes.model.Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
