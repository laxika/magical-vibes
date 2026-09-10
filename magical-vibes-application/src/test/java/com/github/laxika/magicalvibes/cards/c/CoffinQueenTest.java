package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.cards.l.Legerdemain;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.p.PuppetStrings;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoffinQueen.class, Kindle.class, Legerdemain.class, MoggFanatic.class, PuppetStrings.class})
class CoffinQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature card from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(reanimated.getId()));
        assertThat(queen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping Coffin Queen exiles the reanimated creature")
    void untappingExilesReanimatedCreature() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        untapWithPuppetStrings(queen);

        assertThat(queen.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(reanimated.getId()));
        assertExiled(reanimated);
    }

    @Test
    @DisplayName("Coffin Queen leaving the battlefield exiles the reanimated creature")
    void leavingExilesReanimatedCreature() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, queen.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        assertExiled(reanimated);
    }

    @Test
    @DisplayName("A reanimated creature that dies first goes to its owner's graveyard")
    void reanimatedCreatureDiesToItsOwnersGraveyard() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent reanimatedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(reanimated.getId()))
                .findFirst().orElseThrow();
        harness.castInstant(player1, 0, reanimatedPermanent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mogg Fanatic");
        harness.assertNotInGraveyard(player1, "Mogg Fanatic");

        untapWithPuppetStrings(queen);

        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(reanimated.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card kindle = new Kindle();
        harness.setGraveyard(player2, List.of(kindle));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(queen);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, null, kindle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Offers creature cards in both graveyards — the ability targets 'a graveyard'")
    void offersCreatureCardsFromEitherGraveyard() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card ownFanatic = new MoggFanatic();
        Card opponentFanatic = new MoggFanatic();
        harness.setGraveyard(player1, List.of(ownFanatic));
        harness.setGraveyard(player2, List.of(opponentFanatic));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, queen.getCard(), queen.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), gd.playerBattlefields.get(player1.getId()).indexOf(queen));

        assertThat(response.validGraveyardCardIds())
                .containsExactlyInAnyOrder(ownFanatic.getId(), opponentFanatic.getId());
    }

    @Test
    @DisplayName("Choosing not to untap Coffin Queen keeps the reanimated creature")
    void choosingNotToUntapKeepsReanimatedCreature() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(queen.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(reanimated.getId()));
    }

    @Test
    @DisplayName("Choosing to untap Coffin Queen exiles the reanimated creature")
    void choosingToUntapExilesReanimatedCreature() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(queen.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(reanimated.getId()));
        assertExiled(reanimated);
    }

    @Test
    @DisplayName("Losing control of Coffin Queen exiles the reanimated creature")
    void losingControlExilesReanimatedCreature() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        activateReanimate(queen, reanimated);

        Permanent exchangeTarget = addCreatureReady(player2, new MoggFanatic());
        harness.setHand(player2, List.of(new Legerdemain()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, List.of(queen.getId(), exchangeTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(queen);
        assertThat(gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .noneMatch(permanent -> permanent.getCard().getId().equals(reanimated.getId())))
                .isTrue();
        assertExiled(reanimated);
    }

    @Test
    @DisplayName("If Coffin Queen leaves before the ability resolves, the creature is not exiled")
    void leavingBeforeAbilityResolvesDoesNotCreateExileTrigger() {
        Permanent queen = addCreatureReady(player1, new CoffinQueen());
        Card reanimated = new MoggFanatic();
        harness.setGraveyard(player2, List.of(reanimated));

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int queenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(queen);
        harness.activateAbilityWithGraveyardTargets(player1, queenIndex, 0, List.of(reanimated.getId()));

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, queen.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(reanimated.getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(reanimated.getId()));
    }

    private void activateReanimate(Permanent queen, Card graveyardCard) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(queen);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(graveyardCard.getId()));
        resolveAllTriggers();
    }

    private void untapWithPuppetStrings(Permanent target) {
        Permanent strings = harness.addToBattlefieldAndReturn(player1, new PuppetStrings());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int stringsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(strings);
        harness.activateAbility(player1, stringsIndex, 0, null, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();
    }

    private void assertExiled(Card card) {
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(card.getId())
                && entry.ownerId().equals(player2.getId()));
    }
}
