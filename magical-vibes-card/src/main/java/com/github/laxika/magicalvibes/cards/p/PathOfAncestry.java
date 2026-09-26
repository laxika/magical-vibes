package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import java.util.List;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
@CardRegistration(set = "MSC", collectorNumber = "254")
@CardRegistration(set = "SLD", collectorNumber = "250")
@CardRegistration(set = "SLD", collectorNumber = "914")
@CardRegistration(set = "CMM", collectorNumber = "423")
@CardRegistration(set = "CMM", collectorNumber = "661")
@CardRegistration(set = "ECC", collectorNumber = "158")
@CardRegistration(set = "TMC", collectorNumber = "70")
@CardRegistration(set = "SOC", collectorNumber = "393")
public class PathOfAncestry extends Card {

    public PathOfAncestry() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)
                        .withProducingSourceForSpellCastTriggers()),
                "{T}: Add one mana of any color in your commander's color identity. When that mana is spent to cast a creature spell that shares a creature type with your commander, scry 1."
        ));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.usingManaProducedBySource(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSharesCreatureTypeWithCommanderPredicate())),
                        List.of(new ScryEffect(1))));
    }
}
