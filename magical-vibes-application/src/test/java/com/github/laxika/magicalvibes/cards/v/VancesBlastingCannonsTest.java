package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.h.HeadstrongBrute;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VancesBlastingCannons.class, LightningStrike.class, Mountain.class, HeadstrongBrute.class})
class VancesBlastingCannonsTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger exiles top card of library")
    void upkeepExilesTopCard() {
        addCannonsReady(player1);
        Card topCard = new LightningStrike();
        setupTopCard(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Upkeep trigger grants cast permission for nonland card")
    void upkeepGrantsCastPermissionForNonland() {
        addCannonsReady(player1);
        Card topCard = new LightningStrike();
        setupTopCard(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.exilePlayPermissions.get(topCard.getId()))
                .isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topCard.getId());
    }

    @Test
    @DisplayName("Upkeep trigger does NOT grant cast permission for land card")
    void upkeepDoesNotGrantPermissionForLand() {
        addCannonsReady(player1);
        Card land = new Mountain();
        setupTopCard(land);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
    }

    @Test
    @DisplayName("Upkeep trigger with empty library does nothing")
    void upkeepEmptyLibraryDoesNothing() {
        addCannonsReady(player1);
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exile cast permission expires at end of turn")
    void exilePermissionExpiresAtEndOfTurn() {
        addCannonsReady(player1);
        Card topCard = new LightningStrike();
        setupTopCard(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Verify permission is active
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());

        // Advance to cleanup step to clear permissions
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // advance to cleanup

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("Third spell triggers may-transform prompt")
    void thirdSpellTriggersMayTransform() {
        addCannonsReady(player1);

        harness.setHand(player1, List.of(
                new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 6);

        // Cast first spell — no may prompt
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        // Resolve first spell, then cast second
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        // Resolve second spell, then cast third
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        // The optional choice is made when the triggered ability resolves.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may-transform on third spell transforms to Spitfire Bastion")
    void acceptingTransformOnThirdSpell() {
        Permanent cannons = addCannonsReady(player1);

        castThreeSpells();

        // Accept the choice made during resolution.
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the third spell below the triggered ability.
        harness.passBothPriorities();

        assertThat(cannons.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining may-transform on third spell does not transform")
    void decliningTransformKeepsCannons() {
        Permanent cannons = addCannonsReady(player1);

        castThreeSpells();

        // Decline the may transform
        harness.handleMayAbilityChosen(player1, false);

        assertThat(cannons.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger on first or second spell")
    void doesNotTriggerOnFirstOrSecondSpell() {
        addCannonsReady(player1);

        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        // Cast first spell
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();

        // Resolve first, cast second
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Spitfire Bastion tap adds one red mana")
    void spitfireBastionAddsRedMana() {
        Permanent bastion = addTransformedBastion(player1);

        int bastionIdx = indexOf(player1, bastion);
        harness.activateAbility(player1, bastionIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED))
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Spitfire Bastion deals 3 damage to target player")
    void spitfireBastionDeals3DamageToPlayer() {
        Permanent bastion = addTransformedBastion(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int bastionIdx = indexOf(player1, bastion);
        harness.activateAbility(player1, bastionIdx, 1, null, player2.getId());
        harness.passBothPriorities(); // resolve damage ability

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Spitfire Bastion deals 3 damage to target creature")
    void spitfireBastionDeals3DamageToCreature() {
        Permanent bastion = addTransformedBastion(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent creature = addCreatureReady(player2, new HeadstrongBrute());
        java.util.UUID creatureId = creature.getId();

        int bastionIdx = indexOf(player1, bastion);
        harness.activateAbility(player1, bastionIdx, 1, null, creatureId);
        harness.passBothPriorities(); // resolve damage ability

        // 3/3 creature takes 3 damage — should die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creatureId));
        harness.assertInGraveyard(player2, "Headstrong Brute");
    }

    @Test
    @DisplayName("Exiled nonland can be cast by paying its normal mana cost")
    void castsExiledSpellForNormalCost() {
        addCannonsReady(player1);
        Card spell = new LightningStrike();
        setupTopCard(spell);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Strike");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Declining the third-spell transformation does not trigger again on the fourth spell")
    void fourthSpellDoesNotOfferTransformation() {
        Permanent cannons = addCannonsReady(player1);
        castThreeSpells();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(cannons.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Spells cast before Cannons enters count toward the third spell")
    void countsSpellsCastBeforeEntering() {
        harness.setHand(player1, List.of(
                new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent cannons = addCannonsReady(player1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(cannons.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Cannons does not exile a card during an opponent's upkeep")
    void opponentUpkeepDoesNotExile() {
        addCannonsReady(player1);
        Card topCard = new LightningStrike();
        setupTopCard(topCard);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's third spell does not transform Cannons")
    void opponentsThirdSpellDoesNotTransform() {
        Permanent cannons = addCannonsReady(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(
                new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(cannons.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cast permission survives Cannons transforming")
    void exilePermissionSurvivesTransformation() {
        addCannonsReady(player1);
        Card spell = new LightningStrike();
        setupTopCard(spell);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castThreeSpells();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    private Permanent addCannonsReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VancesBlastingCannons());
    }

    private Permanent addTransformedBastion(Player player) {
        Permanent perm = addCannonsReady(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private void setupTopCard(Card card) {
        gd.playerDecks.get(player1.getId()).addFirst(card);
    }

    private void castThreeSpells() {
        harness.setHand(player1, List.of(
                new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
