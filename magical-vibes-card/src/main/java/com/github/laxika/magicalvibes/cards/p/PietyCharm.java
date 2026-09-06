package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "49")
public class PietyCharm extends Card {

    public PietyCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target Aura attached to a creature",
                        new DestroyTargetPermanentEffect(false),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsAuraAttachedToCreaturePredicate(),
                                "Target must be an Aura attached to a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target Soldier creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.SOLDIER))),
                                "Target must be a Soldier creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain vigilance until end of turn",
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES))
        )));
    }
}
