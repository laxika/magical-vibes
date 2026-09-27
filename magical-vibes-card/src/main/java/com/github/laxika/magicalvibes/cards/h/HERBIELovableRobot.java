package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "106")
@CardRegistration(set = "MSC", collectorNumber = "436")
public class HERBIELovableRobot extends Card {

    public HERBIELovableRobot() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new SurveilEffect(1)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));
    }
}
