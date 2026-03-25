package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SpitfireBastion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayCastNonlandThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VancesBlastingCannonsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has upkeep exile trigger")
    void frontFaceHasUpkeepTrigger() {
        VancesBlastingCannons card = new VancesBlastingCannons();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ExileTopCardMayCastNonlandThisTurnEffect.class);
    }

    @Test
    @DisplayName("Front face has third-spell-cast may-transform trigger")
    void frontFaceHasThirdSpellTrigger() {
        VancesBlastingCannons card = new VancesBlastingCannons();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        var mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(NthSpellCastTriggerEffect.class);
        var nthTrigger = (NthSpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(nthTrigger.spellNumber()).isEqualTo(3);
        assertThat(nthTrigger.resolvedEffects()).hasSize(1);
        assertThat(nthTrigger.resolvedEffects().getFirst()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Has back face configured as Spitfire Bastion")
    void hasBackFace() {
        VancesBlastingCannons card = new VancesBlastingCannons();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(SpitfireBastion.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("SpitfireBastion");
    }

    @Test
    @DisplayName("Back face has mana ability and damage ability")
    void backFaceHasCorrectAbilities() {
        VancesBlastingCannons card = new VancesBlastingCannons();
        SpitfireBastion backFace = (SpitfireBastion) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(2);

        // {T}: Add {R}.
        var manaAbility = backFace.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) manaAbility.getEffects().getFirst()).color()).isEqualTo(ManaColor.RED);

        // {2}{R}, {T}: Spitfire Bastion deals 3 damage to any target.
        var damageAbility = backFace.getActivatedAbilities().get(1);
        assertThat(damageAbility.isRequiresTap()).isTrue();
        assertThat(damageAbility.getManaCost()).isEqualTo("{2}{R}");
        assertThat(damageAbility.getEffects()).hasSize(1);
        assertThat(damageAbility.getEffects().getFirst()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) damageAbility.getEffects().getFirst()).damage()).isEqualTo(3);
    }

    // ===== Upkeep exile trigger =====

    @Test
    @DisplayName("Upkeep trigger exiles top card of library")
    void upkeepExilesTopCard() {
        addCannonsReady(player1);
        Card topCard = createNonlandCard("Test Spell");
        setupTopCard(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Test Spell"));
    }

    @Test
    @DisplayName("Upkeep trigger grants cast permission for nonland card")
    void upkeepGrantsCastPermissionForNonland() {
        addCannonsReady(player1);
        Card topCard = createNonlandCard("Castable Spell");
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
        Card land = createLandCard("Exiled Land");
        setupTopCard(land);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Exiled Land"));
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
        Card topCard = createNonlandCard("Expiring Spell");
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

    // ===== Third spell transform trigger =====

    @Test
    @DisplayName("Third spell triggers may-transform prompt")
    void thirdSpellTriggersMayTransform() {
        addCannonsReady(player1);

        harness.setHand(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        // Cast first spell — no may prompt
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();

        // Resolve first bolt, then cast second
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();

        // Resolve second bolt, then cast third
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        // Third spell should trigger may ability prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may-transform on third spell transforms to Spitfire Bastion")
    void acceptingTransformOnThirdSpell() {
        Permanent cannons = addCannonsReady(player1);

        castThreeSpells();

        // Accept the may transform — puts TransformSelfEffect on the stack
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the transform triggered ability
        harness.passBothPriorities();

        assertThat(cannons.isTransformed()).isTrue();
        assertThat(cannons.getCard().getName()).isEqualTo("Spitfire Bastion");
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

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Cast first spell
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();

        // Resolve first, cast second
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    // ===== Back face: Spitfire Bastion abilities =====

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

        Permanent creature = addCreatureReady(player2, 3, 3);
        java.util.UUID creatureId = creature.getId();

        int bastionIdx = indexOf(player1, bastion);
        harness.activateAbility(player1, bastionIdx, 1, null, creatureId);
        harness.passBothPriorities(); // resolve damage ability

        // 3/3 creature takes 3 damage — should die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creatureId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Test Creature"));
    }

    // ===== Helpers =====

    private Permanent addCannonsReady(Player player) {
        VancesBlastingCannons card = new VancesBlastingCannons();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedBastion(Player player) {
        VancesBlastingCannons card = new VancesBlastingCannons();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{G}");
        creature.setColor(CardColor.GREEN);
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createNonlandCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }

    private Card createLandCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }

    private void setupTopCard(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.addFirst(card);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void castThreeSpells() {
        harness.setHand(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve first bolt
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve second bolt
        harness.castInstant(player1, 0, player2.getId());
        // Third spell triggers may prompt — don't resolve yet
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
