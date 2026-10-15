package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "238")
public class TamiyoCompleatedSage extends Card {

    private static final CardAllOfPredicate NONLAND_PERMANENT_WITH_MANA_VALUE_X = new CardAllOfPredicate(List.of(
            new CardIsPermanentPredicate(),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
            new CardMaxManaValueXPredicate(),
            new CardNotPredicate(new CardManaValueLessThanXPredicate())));

    private static final PermanentAnyOfPredicate ARTIFACT_OR_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate()));

    public TamiyoCompleatedSage() {
        TargetFilter artifactOrCreature = new PermanentPredicateTargetFilter(
                ARTIFACT_OR_CREATURE, "Target must be an artifact or creature");
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "+1: Tap up to one target artifact or creature. It doesn't untap during its controller's next untap step.",
                null, +1, null, null,
                List.<TargetFilter>of(artifactOrCreature), 0, 1));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        NONLAND_PERMANENT_WITH_MANA_VALUE_X, true, List.of(), false, false)),
                "\u2212X: Exile target nonland permanent card with mana value X from your graveyard. Create a token that's a copy of that card.",
                null));

        Map<EffectSlot, CardEffect> notebookEffects = Map.of(
                EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                        new CardTruePredicate(), 2, CostModificationScope.SELF));
        CreateTokenEffect notebook = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Tamiyo's Notebook", 0, 0,
                null, null, List.of(CardSubtype.BOOK), Set.of(), Set.of(),
                false, false, notebookEffects,
                List.of(new ActivatedAbility(true, null, List.of(new DrawCardEffect(1)), "{T}: Draw a card.")),
                false, false, true, 0, Set.of());
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(notebook),
                "\u22127: Create Tamiyo's Notebook, a legendary colorless Book artifact token with \"Spells you cast cost {2} less to cast\" and \"{T}: Draw a card.\""));
    }
}
