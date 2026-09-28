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
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "143")
@CardRegistration(set = "SLD", collectorNumber = "966")
@CardRegistration(set = "SLX", collectorNumber = "18")
public class GreymondAvacynsStalwart extends Card {

    private static final String FIRST_STRIKE_VIGILANCE = "First strike and vigilance";
    private static final String FIRST_STRIKE_LIFELINK = "First strike and lifelink";
    private static final String VIGILANCE_LIFELINK = "Vigilance and lifelink";

    public GreymondAvacynsStalwart() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseModeOnEnterEffect(List.of(
                FIRST_STRIKE_VIGILANCE, FIRST_STRIKE_LIFELINK, VIGILANCE_LIFELINK)));

        PermanentHasSubtypePredicate human = new PermanentHasSubtypePredicate(CardSubtype.HUMAN);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(FIRST_STRIKE_VIGILANCE),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                        GrantScope.ALL_OWN_CREATURES, human)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(FIRST_STRIKE_LIFELINK),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK),
                        GrantScope.ALL_OWN_CREATURES, human)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(VIGILANCE_LIFELINK),
                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.LIFELINK),
                        GrantScope.ALL_OWN_CREATURES, human)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(4, human),
                new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES, human)));
    }
}
