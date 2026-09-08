package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "181")
public class JeskaiCharm extends Card {

    public JeskaiCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put target creature on top of its owner's library",
                        new PutTargetOnTopOfLibraryEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Jeskai Charm deals 4 damage to target opponent or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(4, PlayerRelation.OPPONENT),
                        new AnyTargetPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent or planeswalker.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain lifelink until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 1),
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES)))
        )));
    }
}
