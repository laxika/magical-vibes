package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SacrificeUnlessReturnOwnPermanentTypeToHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Sacrifices when no valid permanents on battlefield")
    void sacrificesWhenNoValidPermanents() {
        Card card = createCard("Cloud Spirit");
        Permanent source = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(source);
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        when(gameQueryService.getEffectiveCardTypes(gd, source)).thenReturn(Set.of(CardType.CREATURE));

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport).sacrifice(gd, entry, effect, source.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Presents may ability when valid permanents exist")
    void presentsMayWhenValidPermanentsExist() {
        Card card = createCard("Cloud Spirit");
        Permanent source = new Permanent(card);
        Card landCard = createCard("Island");
        landCard.setType(CardType.LAND);
        Permanent landPerm = new Permanent(landCard);
        gd.playerBattlefields.get(player1Id).addAll(List.of(source, landPerm));
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        when(gameQueryService.getEffectiveCardTypes(gd, source)).thenReturn(Set.of(CardType.CREATURE));
        when(gameQueryService.getEffectiveCardTypes(gd, landPerm)).thenReturn(Set.of(CardType.LAND));

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).isNotEmpty();
        verify(stepDepartureSupport, never()).sacrifice(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Does nothing when source gone and no valid permanents")
    void doesNothingWhenSourceGoneAndNoValidPermanents() {
        Card card = createCard("Cloud Spirit");
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport, never()).sacrifice(any(), any(), any(), any());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
