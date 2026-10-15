package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulOfNewPhyrexia.class, GrizzlyBears.class, Forest.class, Murder.class})
class SoulOfNewPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability grants indestructible to every permanent you control")
    void battlefieldAbilityGrantsIndestructibleToOwnPermanents() {
        harness.addToBattlefield(player1, new SoulOfNewPhyrexia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
        }

        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An indestructible creature survives a destruction spell")
    void indestructibleCreatureSurvivesDestruction() {
        harness.addToBattlefield(player1, new SoulOfNewPhyrexia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SoulOfNewPhyrexia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isFalse();
        }
    }

    @Test
    @DisplayName("Graveyard ability exiles the card and grants indestructible")
    void graveyardAbilityExilesSourceAndGrantsIndestructible() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfNewPhyrexia()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Soul of New Phyrexia");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Soul of New Phyrexia"));

        harness.passBothPriorities();

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();

        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Only permanents present when the battlefield ability resolves are protected")
    void battlefieldAbilityUsesPermanentsAtResolution() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfNewPhyrexia());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, soul, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The battlefield ability resolves even if Soul is destroyed in response")
    void battlefieldAbilityResolvesAfterSourceIsDestroyed() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfNewPhyrexia());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player2, 0, soul.getId());

        harness.assertInGraveyard(player1, "Soul of New Phyrexia");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The graveyard ability can protect lands during an opponent's turn and expires")
    void graveyardAbilityWorksDuringOpponentsTurnAndExpires() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setGraveyard(player1, List.of(new SoulOfNewPhyrexia()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent laterForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.hasKeyword(gd, laterForest, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Insufficient mana cannot exile Soul to activate its graveyard ability")
    void graveyardAbilityRequiresFiveManaBeforeExilingSource() {
        harness.setGraveyard(player1, List.of(new SoulOfNewPhyrexia()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Soul of New Phyrexia");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
