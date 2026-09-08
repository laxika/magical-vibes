package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeiledApparitionTest extends BaseCardTest {

    private Permanent addVeiledApparition() {
        return harness.addToBattlefieldAndReturn(player1, new VeiledApparition());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void animate(Permanent apparition) {
        prepareOpponentCast();
        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
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
    @DisplayName("Does not gain the upkeep ability while it is still an enchantment")
    void doesNotTriggerUpkeepWhileEnchantment() {
        Permanent apparition = addVeiledApparition();

        advanceToUpkeep(player1);
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
}
