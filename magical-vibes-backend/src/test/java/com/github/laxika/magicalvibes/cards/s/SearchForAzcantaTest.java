package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzantaTheSunkenRuin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchForAzcantaTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has correct effects configured")
    void frontFaceHasCorrectEffects() {
        SearchForAzcanta card = new SearchForAzcanta();

        // Two upkeep effects: surveil 1, then conditional transform
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(0))
                .isInstanceOf(SurveilEffect.class);
        SurveilEffect surveil = (SurveilEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(0);
        assertThat(surveil.count()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(1))
                .isInstanceOf(ControllerGraveyardCardThresholdConditionalEffect.class);
        var conditional = (ControllerGraveyardCardThresholdConditionalEffect)
                card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(1);
        assertThat(conditional.threshold()).isEqualTo(7);
        assertThat(conditional.filter()).isNull(); // counts all cards
        assertThat(conditional.wrapped()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) conditional.wrapped();
        assertThat(may.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("AzantaTheSunkenRuin");
    }

    @Test
    @DisplayName("Back face has correct abilities configured")
    void backFaceHasCorrectAbilities() {
        SearchForAzcanta card = new SearchForAzcanta();
        AzantaTheSunkenRuin backFace = (AzantaTheSunkenRuin) card.getBackFaceCard();

        // Two activated abilities: {T}: Add {U} and {2}{U}, {T}: look at top 4
        assertThat(backFace.getActivatedAbilities()).hasSize(2);

        // First: {T}: Add {U}
        var manaAbility = backFace.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        // Second: {2}{U}, {T}: look at top 4
        var lookAbility = backFace.getActivatedAbilities().get(1);
        assertThat(lookAbility.isRequiresTap()).isTrue();
        assertThat(lookAbility.getManaCost()).isEqualTo("{2}{U}");
        assertThat(lookAbility.getEffects()).hasSize(1);
        assertThat(lookAbility.getEffects().getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        var lookEffect = (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect)
                lookAbility.getEffects().getFirst();
        assertThat(lookEffect.count()).isEqualTo(4);
    }

    // ===== Upkeep surveil: accept (put into graveyard) =====

    @Test
    @DisplayName("Surveil puts top card into graveyard when accepted")
    void surveilAccepted() {
        Permanent enchantment = addEnchantmentReady(player1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability — queues surveil may
        harness.handleMayAbilityChosen(player1, true); // accept: put into graveyard

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Upkeep surveil: decline (leave on top) =====

    @Test
    @DisplayName("Surveil leaves card on top when declined")
    void surveilDeclined() {
        Permanent enchantment = addEnchantmentReady(player1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, false); // decline: leave on top

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Grizzly Bears");
    }

    // ===== Transform condition: 7+ cards in graveyard =====

    @Test
    @DisplayName("Transforms after surveil when graveyard reaches 7 cards")
    void transformsWithSevenCardsInGraveyard() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 6 cards in graveyard, plus one on top that surveil will put in graveyard = 7
        fillGraveyard(player1, 6);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true); // surveil: put into graveyard (now 7 in GY)
        // Condition met — may transform prompt appears
        harness.handleMayAbilityChosen(player1, true); // accept transform

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Azcanta, the Sunken Ruin");
    }

    @Test
    @DisplayName("Does not offer transform when graveyard has fewer than 7 cards")
    void noTransformWithFewerThanSevenCards() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 5 cards in graveyard, plus surveil puts 1 more = 6 total (not enough)
        fillGraveyard(player1, 5);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true); // surveil: put into graveyard (now 6)
        // Condition not met — no transform prompt, ability finishes

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Search for Azcanta");
    }

    @Test
    @DisplayName("Does not transform when declining surveil with 6 cards in graveyard")
    void noTransformWhenDecliningSurveilWithSixCards() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 6 cards in graveyard, but decline surveil — stays at 6
        fillGraveyard(player1, 6);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve
        harness.handleMayAbilityChosen(player1, false); // decline surveil — 6 cards, not 7

        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transform is optional (may decline)")
    void transformIsOptional() {
        Permanent enchantment = addEnchantmentReady(player1);

        fillGraveyard(player1, 7); // Already at 7
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve
        harness.handleMayAbilityChosen(player1, false); // decline surveil — still 7
        // Condition met — transform prompt
        harness.handleMayAbilityChosen(player1, false); // decline transform

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Search for Azcanta");
    }

    @Test
    @DisplayName("Trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent enchantment = addEnchantmentReady(player1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        // Advance to opponent's upkeep instead
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(enchantment.isTransformed()).isFalse();
    }

    // ===== Back face: mana ability =====

    @Test
    @DisplayName("Azcanta, the Sunken Ruin taps for blue mana")
    void azantaTapsForBlueMana() {
        Permanent azcanta = addTransformedAzcanta(player1);

        int blueManaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        int azcantaIdx = indexOf(player1, azcanta);
        harness.activateAbility(player1, azcantaIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isEqualTo(blueManaBefore + 1);
    }

    // ===== Helpers =====

    private Permanent addEnchantmentReady(Player player) {
        SearchForAzcanta card = new SearchForAzcanta();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedAzcanta(Player player) {
        SearchForAzcanta card = new SearchForAzcanta();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
    }

    private void fillGraveyard(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = new Card();
            card.setName("Filler Card " + (i + 1));
            card.setType(CardType.INSTANT);
            gd.playerGraveyards.get(player.getId()).add(card);
        }
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
