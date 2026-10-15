package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksWhileSourceControllerHasMostLifePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "113")
@CardRegistration(set = "LCI", collectorNumber = "367")
public class PreacherOfTheSchism extends Card {

    public PreacherOfTheSchism() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAttacksPlayerWithMostLifePredicate(),
                        new CreateTokenEffect("Vampire", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAttacksWhileSourceControllerHasMostLifePredicate(),
                        new DrawCardEffect()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAttacksWhileSourceControllerHasMostLifePredicate(),
                        new LoseLifeEffect(1)));
    }
}
