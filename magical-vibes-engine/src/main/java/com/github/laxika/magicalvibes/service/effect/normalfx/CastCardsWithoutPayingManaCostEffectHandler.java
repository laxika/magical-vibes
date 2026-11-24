package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardsWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CastCardsWithoutPayingManaCostEffect}: offers the face-up spell cards in exile its subject names to
 * the controller in one {@link PendingInteraction.ImprovisationCapstoneCastChoice}, whose answer is the spells to cast
 * in the order to cast them (CR 608.2g). The free-cast queue then casts them one by one, asking each whether it may be
 * cast during the resolution first (CR 601.3), and resumes the resolution once it is empty. Nothing offered, nothing is
 * asked. A total mana-value limit is evaluated here, once (CR 608.2h), and checked against the chosen spells, each
 * split card by the half it is cast as (CR 709.3a). Spell cards the subject names in a graveyard are cast through
 * the graveyard cast flow instead ({@code offerFromGraveyards}).
 */
@Component
@RequiredArgsConstructor
public class CastCardsWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCardsWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cast = (CastCardsWithoutPayingManaCostEffect) effect;
        Set<UUID> offered = new LinkedHashSet<>();
        List<Card> inGraveyards = new ArrayList<>();
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, cast, cast.cards())) {
            ExiledCardEntry exiled = located.zone() == Zone.EXILE
                    ? gameData.findExiledCard(located.card().getId()) : null;
            if (exiled != null && !exiled.faceDown() && isSpell(located.card())) {
                offered.add(located.card().getId());
            } else if (located.zone() == Zone.GRAVEYARD && isSpell(located.card())) {
                inGraveyards.add(located.card());
            }
        }
        if (!inGraveyards.isEmpty()) {
            if (!offered.isEmpty() || cast.totalManaValue() != null) {
                throw new IllegalStateException("Graveyard cards are cast alone and with no total mana value limit");
            }
            offerFromGraveyards(gameData, entry, cast, inGraveyards);
            return;
        }
        if (offered.isEmpty()) {
            return;
        }
        Integer totalManaValue = cast.totalManaValue() == null ? null
                : Math.max(0, amountEvaluationService.evaluate(gameData, cast.totalManaValue(),
                        AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())));
        int maxCount = Math.min(cast.maxCount(), offered.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprovisationCapstoneCastChoice(
                entry.getControllerId(), List.copyOf(offered), maxCount, prompt(cast, maxCount, totalManaValue),
                false, totalManaValue));
    }

    /**
     * Graveyard cards: each is offered with a "may", all in one choice group, of which accepting one withdraws the
     * others. Once a cast begins, the next round offers the cards still there for one spell fewer, so the caster
     * picks the order and a card declined in one round is offered again in the next (CR 608.2g). The rounds are
     * bounded by this round's offers, so a cast that fails can't repeat forever.
     */
    private void offerFromGraveyards(GameData gameData, StackEntry entry, CastCardsWithoutPayingManaCostEffect cast,
                                     List<Card> cards) {
        int remaining = Math.min(cast.maxCount(), cards.size()) - 1;
        CastCardFromGraveyardEffect offer = new CastCardFromGraveyardEffect(new CardTruePredicate(),
                GraveyardSearchScope.ALL_GRAVEYARDS, new CardAnyOfPredicate(List.of()), false, true, null)
                .inChoiceGroup(UUID.randomUUID())
                .thenOffering(remaining <= 0 ? null : new CastCardsWithoutPayingManaCostEffect(cast.cards(), remaining, null));
        for (Card card : cards) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(card, entry.getControllerId(),
                    List.of(offer), entry.getCard().getName() + " — Cast " + card.getName()
                            + " without paying its mana cost?",
                    entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(), entry.getXValue()));
        }
    }

    private static String prompt(CastCardsWithoutPayingManaCostEffect cast, int maxCount, Integer totalManaValue) {
        String spells = cast.maxCount() == Integer.MAX_VALUE
                ? "any number of spells" : "up to " + maxCount + (maxCount == 1 ? " spell" : " spells");
        return "You may cast " + spells + " from among these cards without paying their mana costs."
                + (totalManaValue == null ? "" : " Their total mana value can't exceed " + totalManaValue + ".");
    }

    /** An instant, a sorcery or a nonland permanent card: a land card is played, not cast. */
    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
