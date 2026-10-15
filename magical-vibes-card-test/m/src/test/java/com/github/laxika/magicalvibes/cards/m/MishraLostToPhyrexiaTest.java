package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDragonEngine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishraLostToPhyrexia.class, PhyrexianDragonEngine.class, Forest.class, Mountain.class,
        MachineOverMatter.class})
class MishraLostToPhyrexiaTest extends BaseCardTest {

    @Test
    void enteredAbilityResolvesAllThreeTargetedModesInPrintedOrder() {
        Permanent artifact = addCreatureReady(player2, new PhyrexianDragonEngine());
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new Forest()));
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Destroy target artifact or planeswalker");
        harness.handleListChoice(player1, "Mishra deals 3 damage to any target");
        harness.handleListChoice(player1, "Target opponent discards two cards");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Phyrexian Dragon Engine");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void attackModesMustBeChosenBeforePlayersCanRespond() {
        Permanent mishra = addCreatureReady(player1, new MishraLostToPhyrexia());
        addCreatureReady(player2, new PhyrexianDragonEngine());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mishra)));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        chooseNonTargetingModes();
    }

    @Test
    void enteredModesMustBeChosenBeforePlayersCanRespond() {
        harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        chooseNonTargetingModes();
    }

    @Test
    void modesCannotRepeatAndCreatureEffectsExcludeLaterArrivals() {
        Permanent ownCreature = addCreatureReady(player1, new PhyrexianDragonEngine());
        Permanent opposingCreature = addCreatureReady(player2, new PhyrexianDragonEngine());
        harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());
        resolveAllTriggers();

        String grant = "Creatures you control gain menace and trample until end of turn";
        harness.handleListChoice(player1, grant);
        assertThatThrownBy(() -> harness.handleListChoice(player1, grant))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Creatures you don't control get -1/-1 until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");
        resolveAllTriggers();

        Permanent laterOwnCreature = addCreatureReady(player1, new PhyrexianDragonEngine());
        Permanent laterOpposingCreature = addCreatureReady(player2, new PhyrexianDragonEngine());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterOwnCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterOwnCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(-1);
        assertThat(laterOpposingCreature.getPowerModifier()).isZero();
        assertThat(laterOpposingCreature.getToughnessModifier()).isZero();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    void enteredKeywordModeIncludesMishraItself() {
        Permanent mishra = harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());

        chooseNonTargetingModes();

        assertThat(gqs.hasKeyword(gd, mishra, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mishra, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void attackKeywordModeIncludesMishraItself() {
        Permanent mishra = addCreatureReady(player1, new MishraLostToPhyrexia());
        addCreatureReady(player2, new PhyrexianDragonEngine());

        declareAttackers(List.of(0));
        chooseNonTargetingModes();

        assertThat(gqs.hasKeyword(gd, mishra, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mishra, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void losingTheOnlyTargetPreventsNonTargetingModesFromResolving() {
        Permanent ownCreature = addCreatureReady(player1, new PhyrexianDragonEngine());
        Permanent target = addCreatureReady(player2, new PhyrexianDragonEngine());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());
        harness.handleListChoice(player1, "Destroy target artifact or planeswalker");
        harness.handleListChoice(player1, "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");
        harness.handlePermanentChosen(player1, target.getId());

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Phyrexian Dragon Engine");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void discardModeCannotTargetItsControllerAlongsideAnotherTargetedMode() {
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new MishraLostToPhyrexia());
        harness.handleListChoice(player1, "Target opponent discards two cards");
        harness.handleListChoice(player1, "Mishra deals 3 damage to any target");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
    }

    private void chooseNonTargetingModes() {
        harness.handleListChoice(player1, "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1, "Creatures you don't control get -1/-1 until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");
        resolveAllTriggers();
    }
}