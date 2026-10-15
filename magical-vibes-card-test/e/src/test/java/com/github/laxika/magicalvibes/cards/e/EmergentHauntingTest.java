package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BristlepackSentry;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmergentHaunting.class, BristlepackSentry.class, Island.class})
class EmergentHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a permanent 3/3 Spirit creature with flying at the end step")
    void becomesCreatureAtEndStep() {
        Permanent haunting = addHaunting();

        advanceToEndStep(player1);

        assertThat(gqs.isCreature(gd, haunting)).isTrue();
        assertThat(gqs.getEffectivePower(gd, haunting)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, haunting)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, haunting, Keyword.FLYING)).isTrue();
        assertThat(haunting.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(haunting.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);

        haunting.resetModifiers();

        assertThat(gqs.isCreature(gd, haunting)).isTrue();
        assertThat(gqs.getEffectivePower(gd, haunting)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not animate after casting a spell from hand")
    void doesNotAnimateAfterHandSpell() {
        Permanent haunting = addHaunting();
        harness.setHand(player1, List.of(new BristlepackSentry()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gqs.isCreature(gd, haunting)).isFalse();
    }

    @Test
    @DisplayName("Surveil ability puts the top card into the graveyard when accepted")
    void surveilsOne() {
        addHaunting();
        Card topCard = new BristlepackSentry();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Does not animate during the opponent's end step")
    void doesNotAnimateDuringOpponentsEndStep() {
        Permanent haunting = addHaunting();

        advanceToEndStep(player2);

        assertThat(gqs.isCreature(gd, haunting)).isFalse();
    }

    @Test
    @DisplayName("Already animated haunting does not trigger again")
    void doesNotTriggerWhenAlreadyCreature() {
        addHaunting();
        advanceToEndStep(player1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining surveil preserves the top card and still permits animation")
    void decliningSurveilDoesNotPreventAnimation() {
        Permanent haunting = addHaunting();
        Card topCard = new BristlepackSentry();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);

        advanceToEndStep(player1);

        assertThat(gqs.isCreature(gd, haunting)).isTrue();
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without requesting a choice")
    void surveilEmptyLibrary() {
        addHaunting();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting haunting from hand prevents its animation that turn")
    void castingHauntingPreventsImmediateAnimation() {
        harness.setHand(player1, List.of(new EmergentHaunting()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        Permanent haunting = gd.playerBattlefields.get(player1.getId()).getFirst();

        advanceToEndStep(player1);

        assertThat(gqs.isCreature(gd, haunting)).isFalse();
    }

    private Permanent addHaunting() {
        return harness.addToBattlefieldAndReturn(player1, new EmergentHaunting());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
