package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GenjuOfTheFens.class, Swamp.class, Forest.class, StoneRain.class})
class GenjuOfTheFensTest extends BaseCardTest {

    @Test
    @DisplayName("Genju of the Fens cannot enchant a non-Swamp land")
    void cannotEnchantNonSwamp() {
        harness.addToBattlefield(player1, new Swamp()); // legal target so the spell is castable
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new GenjuOfTheFens()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Swamp");
    }

    @Test
    @DisplayName("Activating {2} makes the enchanted Swamp a 2/2 black Spirit creature that is still a land")
    void activationAnimatesEnchantedSwamp() {
        Permanent swamp = addSwampWithGenju();

        activateGenju();

        assertThat(gqs.isCreature(gd, swamp)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, swamp)).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectiveColors(gd, swamp)).containsExactly(CardColor.BLACK);
        assertThat(gqs.isLand(gd, swamp)).isTrue();
    }

    @Test
    @DisplayName("The animated Swamp gains \"{B}: This creature gets +1/+1 until end of turn\"")
    void animatedSwampGainsPumpAbility() {
        Permanent swamp = addSwampWithGenju();
        activateGenju();

        pumpAnimatedSwamp();

        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(3);
    }

    @Test
    @DisplayName("The granted pump ability can be activated repeatedly")
    void pumpAbilityStacks() {
        Permanent swamp = addSwampWithGenju();
        activateGenju();

        pumpAnimatedSwamp();
        pumpAnimatedSwamp();

        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(4);
    }

    @Test
    @DisplayName("The animation and the granted pump ability wear off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent swamp = addSwampWithGenju();
        activateGenju();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(swamp.getTemporaryActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Destroying the enchanted Swamp lets you return Genju from your graveyard to your hand")
    void returnsFromGraveyardWhenSwampDies() {
        Permanent swamp = addSwampWithGenju();

        destroySwamp(swamp);
        harness.passBothPriorities(); // resolve the "may return" trigger
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Fens");
        harness.assertNotInGraveyard(player1, "Genju of the Fens");
    }

    @Test
    @DisplayName("Declining the trigger leaves Genju in the graveyard")
    void decliningLeavesGenjuInGraveyard() {
        Permanent swamp = addSwampWithGenju();

        destroySwamp(swamp);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Fens");
    }

    @Test
    @DisplayName("The trigger returns only the Genju that enchanted the destroyed Swamp")
    void returnsOnlyTheTriggeringGenju() {
        Permanent swamp = addSwampWithGenju();
        UUID attachedGenjuId = findPermanent(player1, "Genju of the Fens").getCard().getId();
        GenjuOfTheFens otherGenju = new GenjuOfTheFens();
        harness.setGraveyard(player1, List.of(otherGenju));

        destroySwamp(swamp);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(attachedGenjuId))
                .noneMatch(card -> card.getId().equals(otherGenju.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(otherGenju.getId()))
                .noneMatch(card -> card.getId().equals(attachedGenjuId));
    }

    private Permanent addSwampWithGenju() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new GenjuOfTheFens()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, swamp.getId());
        harness.passBothPriorities();

        return swamp;
    }

    private void activateGenju() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Fens"));
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
    }

    private void pumpAnimatedSwamp() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        int swampIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Swamp"));
        harness.activateAbility(player1, swampIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void destroySwamp(Permanent swamp) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, swamp.getId());
        harness.passBothPriorities(); // resolve Stone Rain
    }
}
