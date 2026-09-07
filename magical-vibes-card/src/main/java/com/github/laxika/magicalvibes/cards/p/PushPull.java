package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "250")
public class PushPull extends Card {

    public PushPull() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        PermanentPredicateTargetFilter tappedCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )),
                "Target must be a tapped creature");
        GraveyardCardPredicateTargetFilter singleGraveyardCreature =
                new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.ALL_GRAVEYARDS);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Push — Destroy target tapped creature",
                        new DestroyTargetPermanentEffect(), tappedCreature).withManaCost("{1}{W/B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Pull — Return up to two target creature cards from a single graveyard to the battlefield under your control",
                        List.of(ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromSingleGraveyard(
                                creature, 2, true, true)),
                        singleGraveyardCreature, null, 0, 2, false, null).withManaCost("{4}{B/R}{B/R}")
        )));
    }
}
