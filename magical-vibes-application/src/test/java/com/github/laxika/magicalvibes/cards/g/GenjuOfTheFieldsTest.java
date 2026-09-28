package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

@CardUsed({GenjuOfTheFields.class, Plains.class, Forest.class, StoneRain.class})
class GenjuOfTheFieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Genju of the Fields cannot enchant a non-Plains land")
    void cannotEnchantNonPlains() {
        harness.addToBattlefield(player1, new Plains()); // legal target so the spell is castable
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new GenjuOfTheFields()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Plains");
    }

    @Test
    @DisplayName("Activating {2} makes the enchanted Plains a 2/5 white Spirit creature that is still a land")
    void activationAnimatesEnchantedPlains() {
        Permanent plains = addPlainsWithGenju();

        activateGenju();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, plains)).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.WHITE);
        assertThat(gqs.isLand(gd, plains)).isTrue();
    }

    @Test
    @DisplayName("The animated Plains gains its controller life equal to the damage it deals")
    void animatedPlainsGainsLifeOnDamage() {
        Permanent plains = addPlainsWithGenju();
        activateGenju();
        plains.setSummoningSick(false);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        // Player 2 has no possible blockers, so declaring the attack runs through combat damage.
        declareAttackers(player1, List.of(attackerIndex));
        resolveAllTriggers(); // resolve the granted "whenever this creature deals damage" trigger

        harness.assertLife(player2, opponentLifeBefore - 2);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Each activation grants a separate life-gain ability until end of turn")
    void repeatedActivationsGrantSeparateAbilities() {
        Permanent plains = addPlainsWithGenju();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Fields"));
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
        plains.setSummoningSick(false);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        declareAttackers(player1, List.of(attackerIndex));
        resolveAllTriggers();

        harness.assertLife(player2, opponentLifeBefore - 2);
        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    @DisplayName("The animation and the granted life-gain ability wear off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent plains = addPlainsWithGenju();
        activateGenju();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, plains)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, plains)).isEmpty();
        assertThat(plains.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE)).isEmpty();
    }

    @Test
    @DisplayName("Destroying the enchanted Plains lets you return Genju from your graveyard to your hand")
    void returnsFromGraveyardWhenPlainsDies() {
        Permanent plains = addPlainsWithGenju();

        destroyPlains(plains);
        harness.passBothPriorities(); // resolve the "may return" trigger
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Fields");
        harness.assertNotInGraveyard(player1, "Genju of the Fields");
    }

    @Test
    @DisplayName("Declining the trigger leaves Genju in the graveyard")
    void decliningLeavesGenjuInGraveyard() {
        Permanent plains = addPlainsWithGenju();

        destroyPlains(plains);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Fields");
    }

    private Permanent addPlainsWithGenju() {
        harness.addToBattlefield(player1, new Plains());
        UUID plainsId = harness.getPermanentId(player1, "Plains");
        harness.setHand(player1, List.of(new GenjuOfTheFields()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, plainsId);
        harness.passBothPriorities();

        return findPermanent(player1, "Plains");
    }

    private void activateGenju() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Fields"));
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
    }

    private void destroyPlains(Permanent plains) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, plains.getId());
        harness.passBothPriorities(); // resolve Stone Rain
    }
}
