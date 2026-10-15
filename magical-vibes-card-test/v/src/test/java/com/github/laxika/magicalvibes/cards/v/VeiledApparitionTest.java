package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({VeiledApparition.class, WornPowerstone.class, Annul.class, Conspiracy.class, Clone.class})
class VeiledApparitionTest extends BaseCardTest {

    private Permanent addVeiledApparition() {
        return harness.addToBattlefieldAndReturn(player1, new VeiledApparition());
    }

    private void prepareCast(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void animate(Permanent apparition) {
        prepareCast(player2);
        harness.castFromHand(player2, new WornPowerstone(), "{3}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, apparition)).isTrue();
    }

    @Test
    @DisplayName("Becomes a 3/3 Illusion creature with flying when an opponent casts a spell")
    void becomesIllusionCreatureWhenOpponentCastsSpell() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);

        assertThat(gqs.isEnchantment(gd, apparition)).isFalse();
        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, apparition)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, apparition)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.hasKeyword(gd, apparition, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerWhenItsControllerCastsSpell() {
        Permanent apparition = addVeiledApparition();
        prepareCast(player1);

        harness.castFromHand(player1, new WornPowerstone(), "{3}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, apparition)).isTrue();
        assertThat(gqs.isCreature(gd, apparition)).isFalse();
    }

    @Test
    @DisplayName("Triggers when the opponent casts a spell even if that spell is countered")
    void triggersWhenOpponentSpellIsCountered() {
        Permanent apparition = addVeiledApparition();
        prepareCast(player2);

        WornPowerstone powerstone = new WornPowerstone();
        harness.castFromHand(player2, powerstone, "{3}");
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Annul()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, powerstone.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, apparition)).isTrue();
        harness.assertInGraveyard(player2, "Worn Powerstone");
    }

    @Test
    @DisplayName("Does not gain the upkeep ability while it is still an enchantment")
    void doesNotTriggerUpkeepWhileEnchantment() {
        Permanent apparition = addVeiledApparition();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apparition);
    }

    @Test
    @DisplayName("The upkeep ability triggers only during its controller's upkeep")
    void upkeepAbilityTriggersOnlyDuringItsControllersUpkeep() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apparition);
    }

    @Test
    @DisplayName("Paying {1}{U} keeps the Illusion on the battlefield during upkeep")
    void payingUpkeepKeepsIllusion() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apparition);
    }

    @Test
    @DisplayName("Declining to pay {1}{U} sacrifices the Illusion during upkeep")
    void decliningUpkeepPaymentSacrificesIllusion() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apparition);
        harness.assertInGraveyard(player1, "Veiled Apparition");
    }

    @Test
    @DisplayName("Transformation resolves before the spell that triggered it")
    void transformsBeforeOpponentSpellResolves() {
        Permanent apparition = addVeiledApparition();
        prepareCast(player2);
        harness.castFromHand(player2, new WornPowerstone(), "{3}");

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, apparition)).isTrue();
        harness.assertNotOnBattlefield(player2, "Worn Powerstone");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Worn Powerstone");
    }

    @Test
    @DisplayName("An animated Apparition does not trigger again for another opponent spell")
    void doesNotTriggerAgainAfterTransformation() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);
        resolveAllTriggers();
        prepareCast(player2);

        harness.castFromHand(player2, new WornPowerstone(), "{3}");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Worn Powerstone")).isEqualTo(2);
        assertThat(gqs.isCreature(gd, apparition)).isTrue();
    }

    @Test
    @DisplayName("The upkeep payment can use one blue and one colorless mana")
    void upkeepPaymentAcceptsGenericMana() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apparition);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Changing the animated creature's type does not remove its upkeep ability")
    void retainsUpkeepAbilityAfterCreatureTypeChanges() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);
        resolveAllTriggers();
        prepareCast(player1);
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        assertThat(gqs.effectiveCreatureSubtypes(gd, apparition)).containsExactly(CardSubtype.GOBLIN);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Veiled Apparition");
    }

    @Test
    @DisplayName("Copying an animated Apparition copies the enchantment without its animation")
    void copyingAnimatedApparitionDoesNotCopyAnimation() {
        Permanent apparition = addVeiledApparition();
        animate(apparition);
        resolveAllTriggers();
        prepareCast(player2);
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, apparition.getId());

        Permanent copy = findPermanent(player2, "Veiled Apparition");
        assertThat(gqs.isEnchantment(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
    }
}
