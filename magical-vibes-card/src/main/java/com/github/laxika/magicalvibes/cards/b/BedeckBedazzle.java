package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "221")
public class BedeckBedazzle extends Card {

    public BedeckBedazzle() {
        TargetFilter creature = TargetFilters.creature();
        TargetFilter nonbasicLand = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                )),
                "Target must be a nonbasic land."
        );
        TargetFilter opponentOrPlaneswalker = new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker."
        );

        CardEffect bedeck = new BoostTargetCreatureEffect(3, -3);
        CardEffect destroyNonbasicLand = new DestroyTargetPermanentEffect();
        CardEffect dealDamage = new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bedeck — Target creature gets +3/-3 until end of turn",
                        bedeck,
                        creature
                ).withManaCost("{B/R}{B/R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Bedazzle — Destroy target nonbasic land. Bedazzle deals 2 damage to target opponent or planeswalker",
                        List.of(destroyNonbasicLand, dealDamage),
                        List.of(nonbasicLand, opponentOrPlaneswalker)
                ).withManaCost("{4}{B}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Bedeck and then Bedazzle",
                        List.of(bedeck, destroyNonbasicLand, dealDamage),
                        List.of(creature, nonbasicLand, opponentOrPlaneswalker)
                ).withManaCost("{4}{B/R}{B/R}{B}{R}")
        )));
    }
}
