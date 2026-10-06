package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfCourage;
import com.github.laxika.magicalvibes.cards.e.EpharasRadiance;
import com.github.laxika.magicalvibes.cards.o.OreskosSunGuide;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentSentinel.class, ArchetypeOfCourage.class, EpharasRadiance.class, OreskosSunGuide.class})
class SilentSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers an enchantment card from your graveyard")
    void attackOffersEnchantmentFromOwnGraveyard() {
        Card insight = new ArchetypeOfCourage();
        harness.setGraveyard(player1, List.of(insight));
        addReadySilentSentinel();

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(insight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();
        acceptReturnIfOffered();

        harness.assertOnBattlefield(player1, "Archetype of Courage");
        harness.assertNotInGraveyard(player1, "Archetype of Courage");
    }

    @Test
    @DisplayName("Nonenchantment cards are not legal attack targets")
    void attackDoesNotOfferNonenchantmentCards() {
        harness.setGraveyard(player1, List.of(new OreskosSunGuide()));
        addReadySilentSentinel();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Oreskos Sun Guide");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the enchantment in the graveyard")
    void decliningAttackTriggerLeavesGraveyardUntouched() {
        Card insight = new ArchetypeOfCourage();
        harness.setGraveyard(player1, List.of(insight));
        addReadySilentSentinel();

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(insight.getId());
        harness.assertNotOnBattlefield(player1, "Archetype of Courage");
    }

    private Permanent addReadySilentSentinel() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SilentSentinel());
        sentinel.setSummoningSick(false);
        return sentinel;
    }

    private void acceptReturnIfOffered() {
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    @Test
    void attackRequiresATargetEvenIfReturnWillBeDeclined() {
        Card enchantment = new ArchetypeOfCourage();
        harness.setGraveyard(player1, List.of(enchantment));
        addReadySilentSentinel();
        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentGraveyardIsNotOffered() {
        Card own = new ArchetypeOfCourage();
        Card opposing = new ArchetypeOfCourage();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opposing));
        addReadySilentSentinel();
        declareAttackers(List.of(0));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(own.getId());
    }

    @Test
    void targetRemovedBeforeResolutionIsNotReturned() {
        Card enchantment = new ArchetypeOfCourage();
        harness.setGraveyard(player1, List.of(enchantment));
        addReadySilentSentinel();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.setGraveyard(player1, List.of());
        gd.getPlayerExiledCards(player1.getId()).add(enchantment);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Archetype of Courage");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(enchantment);
    }

    @Test
    void returnedAuraOffersAttachmentAsItEnters() {
        Card aura = new EpharasRadiance();
        harness.setGraveyard(player1, List.of(aura));
        Permanent sentinel = addReadySilentSentinel();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        acceptReturnIfOffered();
        assertThat(gd.interaction.pendingAuraCard()).isSameAs(aura);
        harness.handlePermanentChosen(player1, sentinel.getId());
        harness.assertOnBattlefield(player1, "Ephara's Radiance");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(aura.getId()))
                .singleElement().extracting(Permanent::getAttachedTo).isEqualTo(sentinel.getId());
    }
}
