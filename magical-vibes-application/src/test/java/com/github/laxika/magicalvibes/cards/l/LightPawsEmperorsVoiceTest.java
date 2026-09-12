package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LightPawsEmperorsVoice.class)
class LightPawsEmperorsVoiceTest extends BaseCardTest {

    @Test
    void castAuraSearchesForEligibleAuraAndAttachesItToLightPaws() {
        Permanent lightPaws = addCreatureReady(player1, new LightPawsEmperorsVoice());
        Card enteringAura = aura("Entering Aura", "{2}");
        Card eligibleAura = aura("Eligible Aura", "{2}");
        Card sameNameAura = aura("Entering Aura", "{1}");
        Card tooExpensiveAura = aura("Too Expensive Aura", "{3}");
        Card creature = card("Creature", CardType.CREATURE, "{1}");

        harness.setHand(player1, List.of(enteringAura));
        harness.setLibrary(player1, List.of(eligibleAura, sameNameAura, tooExpensiveAura, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, lightPaws.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(eligibleAura);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT);
        assertThat(search.params().attachToPermanentId()).isEqualTo(lightPaws.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligibleAura
                        && lightPaws.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(sameNameAura, tooExpensiveAura, creature);
    }

    @Test
    void auraPutOntoBattlefieldWithoutBeingCastDoesNotTriggerSearch() {
        addCreatureReady(player1, new LightPawsEmperorsVoice());
        Card enteringAura = aura("Put Aura", "{1}");

        harness.setLibrary(player1, List.of(aura("Eligible Aura", "{1}")));
        harness.enterBattlefieldAndReturn(player1, enteringAura);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card aura(String name, String manaCost) {
        Card card = card(name, CardType.ENCHANTMENT, manaCost);
        card.setSubtypes(List.of(CardSubtype.AURA));
        card.target(TargetFilters.creature());
        return card;
    }

    private Card card(String name, CardType type, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        return card;
    }
}
