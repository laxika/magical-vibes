package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberheartChallenger.class, Forest.class, GiantGrowth.class, BraveKinDuo.class})
class EmberheartChallengerTest extends BaseCardTest {

    @Test
    @DisplayName("Valiant exiles the top card with permission to play it this turn")
    void valiantExilesTopCardWithPlayPermission() {
        Permanent challenger = addChallenger();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castGrowth(player1, challenger);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Valiant triggers only once each turn")
    void valiantTriggersOnlyOnceEachTurn() {
        Permanent challenger = addChallenger();
        Forest firstTopCard = new Forest();
        Forest secondTopCard = new Forest();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));

        castGrowth(player1, challenger);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, challenger.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondTopCard);
    }

    @Test
    @DisplayName("Valiant does not trigger for an opponent's spell")
    void valiantDoesNotTriggerForOpponentsSpell() {
        Permanent challenger = addChallenger();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, challenger.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A controlled activated ability triggers valiant and the exiled land can be played")
    void controlledAbilityAllowsPlayingExiledLand() {
        Permanent challenger = addChallenger();
        addCreatureReady(player1, new BraveKinDuo());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, challenger.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gqs.getEffectivePower(gd, challenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, challenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent targeting first does not consume the controlled targeting trigger")
    void opponentTargetingDoesNotConsumeValiant() {
        Permanent challenger = addChallenger();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castGrowth(player2, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        castGrowth(player1, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gqs.getEffectivePower(gd, challenger)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, challenger)).isEqualTo(9);
    }

    @Test
    @DisplayName("An empty library still consumes the first controlled targeting event")
    void emptyLibraryStillConsumesValiant() {
        Permanent challenger = addChallenger();
        harness.setLibrary(player1, List.of());
        castGrowth(player1, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castGrowth(player1, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Exile permission expires and valiant becomes available on the next turn")
    void permissionExpiresAndValiantResetsNextTurn() {
        Permanent challenger = addChallenger();
        Forest firstCard = new Forest();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard));
        castGrowth(player1, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(firstCard.getId());

        harness.setLibrary(player1, List.of(nextCard));
        castGrowth(player1, challenger);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard, nextCard);
        assertThat(gd.exilePlayPermissions.get(nextCard.getId())).isEqualTo(player1.getId());
    }
    private Permanent addChallenger() {
        return harness.addToBattlefieldAndReturn(player1, new EmberheartChallenger());
    }

    private void castGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player, 0, target.getId());
    }
}
