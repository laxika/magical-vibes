package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkkiEmberKeeper;
import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SokenzanSmelter.class, AutomatedArtificer.class, AkkiEmberKeeper.class})
class SokenzanSmelterTest extends BaseCardTest {

    @Test
    void paysAndSacrificesArtifactToCreateHastyConstruct() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());

        resolveBeginningOfCombatTrigger();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Automated Artificer");
        Permanent token = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(countPermanents(player1, "Construct")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void declineLeavesArtifactAndCreatesNoToken() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        harness.addToBattlefield(player1, new AutomatedArtificer());

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Automated Artificer");
        harness.assertNotOnBattlefield(player1, "Construct");
    }

    @Test
    void onlyArtifactsCanBeChosenForTheSacrifice() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AkkiEmberKeeper());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId())
                .doesNotContain(creature.getId(), opposingArtifact.getId());
    }

    @Test
    void createsTokenDuringTheOriginalAbilityResolution() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handlePermanentChosen(player1, artifact.getId()));

        harness.assertInGraveyard(player1, "Automated Artificer");
        harness.assertOnBattlefield(player1, "Construct");
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Construct")).isEqualTo(1);
    }

    @Test
    void cannotCreateTokenWithoutMana() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        harness.addToBattlefield(player1, new AutomatedArtificer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Automated Artificer");
        harness.assertNotOnBattlefield(player1, "Construct");
    }

    @Test
    void cannotPayWhenOnlyOpponentControlsAnArtifact() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        harness.addToBattlefield(player2, new AutomatedArtificer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        harness.assertOnBattlefield(player2, "Automated Artificer");
        harness.assertNotOnBattlefield(player1, "Construct");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new SokenzanSmelter());
        harness.addToBattlefield(player1, new AutomatedArtificer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Construct");
    }

    private void resolveBeginningOfCombatTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
