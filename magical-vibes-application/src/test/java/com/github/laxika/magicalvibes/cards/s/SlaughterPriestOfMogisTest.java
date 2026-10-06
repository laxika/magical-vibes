package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheForge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterPriestOfMogis.class, DiabolicEdict.class, GrizzlyBears.class, GloriousAnthem.class,
        OmenOfTheForge.class})
class SlaughterPriestOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 whenever you sacrifice a permanent")
    void getsBoostWhenControllerSacrificesPermanent() {
        Permanent priest = addReadyPriest(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent sacrifices a permanent")
    void doesNotBoostWhenOpponentSacrificesPermanent() {
        Permanent priest = addReadyPriest(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing another creature grants first strike")
    void sacrificesAnotherCreatureAndGainsFirstStrike() {
        Permanent priest = addReadyPriest(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), enchantment.getId());
        assertThat(choice.validIds()).doesNotContain(priest.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(priest.getEffectivePower()).isEqualTo(5);
        assertThat(priest.getEffectiveToughness()).isEqualTo(3);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike and the boost wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent priest = addReadyPriest(player1);
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature or an enchantment")
    void cannotActivateWithoutSacrifice() {
        Permanent priest = addReadyPriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(priest), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @CardUsed({SlaughterPriestOfMogis.class, OmenOfTheForge.class})
    @DisplayName("A tapped, summoning-sick priest can sacrifice a noncreature enchantment")
    void tappedSummoningSickPriestCanActivate() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new SlaughterPriestOfMogis());
        priest.setSummoningSick(true);
        priest.setTapped(true);
        harness.addToBattlefield(player1, new OmenOfTheForge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Omen of the Forge");
        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.getEffectiveToughness()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(priest.isTapped()).isTrue();
    }

    @Test
    @CardUsed({SlaughterPriestOfMogis.class, OmenOfTheForge.class})
    @DisplayName("Repeated activations each boost power even after first strike is gained")
    void repeatedActivationsAccumulateBoosts() {
        Permanent priest = addReadyPriest(player1);
        Permanent firstOmen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheForge());
        harness.addToBattlefield(player1, new OmenOfTheForge());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        harness.handlePermanentChosen(player1, firstOmen.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        resolveAllTriggers();

        assertThat(priest.getEffectivePower()).isEqualTo(6);
        assertThat(priest.getEffectiveToughness()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({SlaughterPriestOfMogis.class, OmenOfTheForge.class})
    @DisplayName("Every priest triggers, but only the activated priest gains first strike")
    void sacrificeTriggersEachPriest() {
        Permanent priest = addReadyPriest(player1);
        Permanent otherPriest = addReadyPriest(player1);
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheForge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        harness.handlePermanentChosen(player1, omen.getId());
        resolveAllTriggers();

        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(otherPriest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(otherPriest.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @CardUsed({SlaughterPriestOfMogis.class, OmenOfTheForge.class})
    @DisplayName("The ability cannot be activated with only one mana")
    void cannotActivateWithoutTwoMana() {
        Permanent priest = addReadyPriest(player1);
        harness.addToBattlefield(player1, new OmenOfTheForge());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(priest), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Omen of the Forge");
        assertThat(priest.getEffectivePower()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyPriest(Player player) {
        return addCreatureReady(player, new SlaughterPriestOfMogis());
    }

    @Test
    @CardUsed({SlaughterPriestOfMogis.class, OmenOfTheForge.class})
    @DisplayName("Sacrificing an enchantment to its own ability boosts the priest without granting first strike")
    void sacrificeToOtherAbilityOnlyGrantsBoost() {
        Permanent priest = addReadyPriest(player1);
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheForge());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(omen), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Omen of the Forge");
        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.getEffectiveToughness()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
    }
}
