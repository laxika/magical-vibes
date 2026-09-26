package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "66")
@CardRegistration(set = "LTC", collectorNumber = "147")
public class RadagastWizardOfWilds extends Card {

    private static final String BEAST_MODE = "Create a 3/3 green Beast creature token";
    private static final String BIRD_MODE = "Create a 2/2 blue Bird creature token with flying";

    public RadagastWizardOfWilds() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));

        PermanentAnyOfPredicate beastOrBird = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.BEAST),
                new PermanentHasSubtypePredicate(CardSubtype.BIRD)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.WARD, GrantScope.OWN_CREATURES, beastOrBird));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(1), GrantScope.OWN_CREATURES, beastOrBird));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(5),
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(BEAST_MODE,
                                new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN,
                                        List.of(CardSubtype.BEAST), Set.of(), Set.of())),
                        new ChooseOneEffect.ChooseOneOption(BIRD_MODE,
                                new CreateTokenEffect("Bird", 2, 2, CardColor.BLUE,
                                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()))
                )))
        ));
    }
}
