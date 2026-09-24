package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "143")
@CardRegistration(set = "SLD", collectorNumber = "966")
public class GreymondAvacynsStalwart extends Card {

    private static final String FIRST_STRIKE = "First strike";
    private static final String VIGILANCE = "Vigilance";
    private static final String LIFELINK = "Lifelink";

    public GreymondAvacynsStalwart() {
        PermanentHasSubtypePredicate human = new PermanentHasSubtypePredicate(CardSubtype.HUMAN);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(FIRST_STRIKE, VIGILANCE, LIFELINK), false, 2));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(FIRST_STRIKE),
                new StaticBoostEffect(0, 0, Set.of(Keyword.FIRST_STRIKE), GrantScope.ALL_OWN_CREATURES, human)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(VIGILANCE),
                new StaticBoostEffect(0, 0, Set.of(Keyword.VIGILANCE), GrantScope.ALL_OWN_CREATURES, human)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(LIFELINK),
                new StaticBoostEffect(0, 0, Set.of(Keyword.LIFELINK), GrantScope.ALL_OWN_CREATURES, human)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(4, human),
                new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES, human)));
    }
}
