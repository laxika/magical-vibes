package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "145")
@CardRegistration(set = "SPM", collectorNumber = "215")
public class SilkWebWeaver extends Card {

    public SilkWebWeaver() {
        PermanentAllOfPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{1}{G}{W}"),
                new ReturnPermanentsCost(1, tappedCreature))));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new CreateTokenEffect(
                        1, "Human Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.HUMAN, CardSubtype.CITIZEN)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{W}",
                List.of(
                        new BoostAllOwnCreaturesEffect(2, 2),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{3}{G}{W}: Creatures you control get +2/+2 and gain vigilance until end of turn."));
    }
}
