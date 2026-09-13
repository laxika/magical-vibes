package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokenzanSmelter.class, Spellbook.class, GrizzlyBears.class})
class SokenzanSmelterTest extends BaseCardTest {

    @Test
    void paysAndSacrificesArtifactToCreateHastyConstruct() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        resolveBeginningOfCombatTrigger();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        Permanent token = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void declineLeavesArtifactAndCreatesNoToken() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        harness.addToBattlefield(player1, new Spellbook());

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Construct");
    }

    @Test
    void onlyArtifactsCanBeChosenForTheSacrifice() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId())
                .doesNotContain(creature.getId());
    }

    private void resolveBeginningOfCombatTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
