package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KravensCats;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacularSpiderMan.class, KravensCats.class})
class SpectacularSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 0, null, null);
        harness.passBothPriorities();

        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice ability grants hexproof and indestructible to your creatures")
    void sacrificeGrantsHexproofAndIndestructibleToOwnCreatures() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        Permanent ally = addCreatureReady(player1, new KravensCats());
        Permanent opposing = addCreatureReady(player2, new KravensCats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectacular Spider-Man");
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposing.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opposing.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void canBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SpectacularSpiderMan(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spectacular Spider-Man");
        harness.assertNotInHand(player1, "Spectacular Spider-Man");
    }

    @Test
    @DisplayName("A summoning-sick tapped Spider-Man can activate the flying ability")
    void flyingAbilityDoesNotRequireTappingOrHaste() {
        Permanent spiderMan = harness.addToBattlefieldAndReturn(player1, new SpectacularSpiderMan());
        spiderMan.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 0, null, null);
        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, and protection waits for resolution")
    void sacrificeIsAnActivationCost() {
        Permanent spiderMan = harness.addToBattlefieldAndReturn(player1, new SpectacularSpiderMan());
        spiderMan.tap();
        Permanent ally = addCreatureReady(player1, new KravensCats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 1, null, null);

        harness.assertNotOnBattlefield(player1, "Spectacular Spider-Man");
        harness.assertInGraveyard(player1, "Spectacular Spider-Man");
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Protection applies to creatures present at resolution, excluding later arrivals")
    void protectionLocksInCreaturesAtResolution() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, spiderMan), 1, null, null);

        Permanent beforeResolution = addCreatureReady(player1, new KravensCats());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new KravensCats());

        assertThat(beforeResolution.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(afterResolution.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A pending flying ability does not grant flying to another Spider-Man")
    void flyingDoesNotFollowANewCopyOfTheSource() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, spiderMan), 0, null, null);
        harness.activateAbility(player1, indexOf(player1, spiderMan), 1, null, null);
        harness.passBothPriorities();

        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new SpectacularSpiderMan());
        harness.passBothPriorities();

        assertThat(replacement.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(replacement.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(replacement.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
