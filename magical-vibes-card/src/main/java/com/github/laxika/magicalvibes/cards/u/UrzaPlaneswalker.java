package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "238b")
public class UrzaPlaneswalker extends Card {

    public UrzaPlaneswalker() {
        addEffect(EffectSlot.STATIC, new AllowExtraLoyaltyActivationEffect());

        CardAnyOfPredicate artifactInstantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(artifactInstantOrSorcery, 2),
                        new GainLifeEffect(2)),
                "+2: Artifact, instant, and sorcery spells you cast this turn cost {2} less to cast. You gain 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(2), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "+1: Draw two cards, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 2, "Soldier", 1, 1, null, Set.of(),
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT),
                        false, false, Map.of(), List.of(), false, false, false, 0, Set.of()
                )),
                "0: Create two 1/1 colorless Soldier artifact creature tokens."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentEffect()),
                "−3: Exile target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsPlaneswalkerPredicate()))),
                        new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate()))
                ),
                "−10: Artifacts and planeswalkers you control gain indestructible until end of turn. "
                        + "Destroy all nonland permanents."
        ));
    }
}
