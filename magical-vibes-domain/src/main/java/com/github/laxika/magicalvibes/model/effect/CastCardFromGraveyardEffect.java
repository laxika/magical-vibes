package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * "You may cast [a card] from your graveyard" during the resolution: the controller is offered the cast, for the
 * card's mana cost or without paying it ({@link #withoutPayingManaCost}, an alternative cost, CR 118.9; additional
 * costs are still paid, CR 118.8). The cast is asked through {@code CastingPermissionService} before it begins
 * (CR 601.3, 608.2g).
 *
 * <p>Which card: with {@link #cards} set, the cards that subject names in a graveyard ("cast this card",
 * {@code Subjects.sourceCard()}; "cast it" of a discard trigger, {@code triggeringCards(filter)}; "cast target
 * [filter] card from your graveyard", {@code targetCard(filter, scope)}, which declares the target), each one
 * offered on its own with a "may" (several are offered as one choice: accepting one withdraws the others). Without
 * it, the controller chooses one card matching {@link #filter} in the graveyards of {@link #scope} as the effect
 * resolves. Either way the card must still match {@link #filter} and be in a graveyard of {@link #scope} when the
 * cast is accepted.
 *
 * @param filter                        which cards may be cast
 * @param scope                         whose graveyards
 * @param exileInsteadOfGraveyardFilter the cast spells that are exiled instead of being put into a graveyard
 * @param allowAdventure                whether an adventurer card may be cast as its Adventure, judged by the
 *                                      Adventure's characteristics (CR 716.3a)
 * @param withoutPayingManaCost         whether the spell is cast without paying its mana cost
 * @param afterSuccessfulCastEffect     "if you do": resolved as part of this ability once the spell is cast
 * @param anyManaType                   mana of any type can be spent to cast the spell (Tinybones, Quistis Trepe)
 * @param cards                         the cards offered, or {@code null} for a choice among the matching cards
 * @param choiceGroupId                 engine-built: offers made together, of which only one may be cast
 * @param nextOffers                    engine-built: the next round of an "any number" choice, made as part of
 *                                      the resolving ability once this cast begins, so the caster casts the spells
 *                                      in the order of their choice (CR 608.2g)
 */
public record CastCardFromGraveyardEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        CardPredicate exileInsteadOfGraveyardFilter,
        boolean allowAdventure,
        boolean withoutPayingManaCost,
        CardEffect afterSuccessfulCastEffect,
        boolean anyManaType,
        CardSubject cards,
        UUID choiceGroupId,
        CardEffect nextOffers
) implements CardEffect {

    public CastCardFromGraveyardEffect(
            CardPredicate filter,
            GraveyardSearchScope scope,
            CardPredicate exileInsteadOfGraveyardFilter,
            boolean allowAdventure,
            boolean withoutPayingManaCost,
            CardEffect afterSuccessfulCastEffect,
            boolean anyManaType) {
        this(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure, withoutPayingManaCost,
                afterSuccessfulCastEffect, anyManaType, null, null, null);
    }

    public CastCardFromGraveyardEffect(
            CardPredicate filter,
            GraveyardSearchScope scope,
            CardPredicate exileInsteadOfGraveyardFilter,
            boolean allowAdventure,
            boolean withoutPayingManaCost,
            CardEffect afterSuccessfulCastEffect) {
        this(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure, withoutPayingManaCost,
                afterSuccessfulCastEffect, false);
    }

    public CastCardFromGraveyardEffect(
            CardPredicate filter,
            GraveyardSearchScope scope,
            CardPredicate exileInsteadOfGraveyardFilter,
            boolean allowAdventure) {
        this(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure, false, null);
    }

    public CastCardFromGraveyardEffect(
            CardPredicate filter,
            GraveyardSearchScope scope,
            CardPredicate exileInsteadOfGraveyardFilter,
            boolean allowAdventure,
            boolean withoutPayingManaCost) {
        this(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure, withoutPayingManaCost, null);
    }

    public CastCardFromGraveyardEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(exileInsteadOfGraveyardFilter, "exileInsteadOfGraveyardFilter");
    }

    /**
     * "You may cast {@code cards} from your graveyard", for its mana cost, as itself (not as an Adventure), put into a
     * graveyard as usual. A land card is never offered (it is played, not cast). A target subject's scope is the
     * graveyards the card may be in.
     */
    public static CastCardFromGraveyardEffect of(CardSubject cards) {
        Objects.requireNonNull(cards, "cards");
        GraveyardSearchScope scope = cards instanceof CardSubject.TargetCard target
                ? target.scope() : GraveyardSearchScope.ALL_GRAVEYARDS;
        return new CastCardFromGraveyardEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND)), scope,
                new CardAnyOfPredicate(List.of()), false, false, null, false, cards, null, null);
    }

    /** "… without paying its mana cost." */
    public CastCardFromGraveyardEffect withoutPayingItsManaCost() {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure, true,
                afterSuccessfulCastEffect, anyManaType, cards, choiceGroupId, nextOffers);
    }

    /** "… and mana of any type can be spent to cast that spell." */
    public CastCardFromGraveyardEffect spendingManaOfAnyType() {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure,
                withoutPayingManaCost, afterSuccessfulCastEffect, true, cards, choiceGroupId, nextOffers);
    }

    /** "If that spell would be put into your graveyard, exile it instead." */
    public CastCardFromGraveyardEffect exilingInsteadOfGraveyard() {
        return new CastCardFromGraveyardEffect(filter, scope, new CardTruePredicate(), allowAdventure,
                withoutPayingManaCost, afterSuccessfulCastEffect, anyManaType, cards, choiceGroupId, nextOffers);
    }

    /** An adventurer card may also be cast as its Adventure (CR 716.3a). */
    public CastCardFromGraveyardEffect allowingAdventure() {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, true,
                withoutPayingManaCost, afterSuccessfulCastEffect, anyManaType, cards, choiceGroupId, nextOffers);
    }

    /** "If you do, {@code effect}": resolved as part of this ability once the spell is cast. */
    public CastCardFromGraveyardEffect ifCast(CardEffect effect) {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure,
                withoutPayingManaCost, Objects.requireNonNull(effect, "effect"), anyManaType, cards, choiceGroupId,
                nextOffers);
    }

    /** Engine-built: this offer one of {@code groupId}'s, of which accepting one withdraws the others. */
    public CastCardFromGraveyardEffect inChoiceGroup(UUID groupId) {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure,
                withoutPayingManaCost, afterSuccessfulCastEffect, anyManaType, cards, groupId, nextOffers);
    }

    /** Engine-built: this offer followed, once its cast begins, by {@code offers} (CR 608.2g). */
    public CastCardFromGraveyardEffect thenOffering(CardEffect offers) {
        return new CastCardFromGraveyardEffect(filter, scope, exileInsteadOfGraveyardFilter, allowAdventure,
                withoutPayingManaCost, afterSuccessfulCastEffect, anyManaType, cards, choiceGroupId, offers);
    }

    @Override
    public TargetSpec targetSpec() {
        return cards == null ? TargetSpec.NONE : cards.targetSpec();
    }
}
