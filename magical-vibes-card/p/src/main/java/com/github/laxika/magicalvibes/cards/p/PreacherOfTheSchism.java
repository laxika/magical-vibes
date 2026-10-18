package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksWhileSourceControllerHasMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "113")
@CardRegistration(set = "LCI", collectorNumber = "367")
public class PreacherOfTheSchism extends Card {

    public PreacherOfTheSchism() {
        // Whenever this creature attacks the player with the most life or tied for most life, create a
        // 1/1 white Vampire creature token with lifelink.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourcePermanentPredicate(),
                                new PermanentAttacksPlayerWithMostLifePredicate())),
                        new CreateTokenEffect("Vampire", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())),
                TriggerMode.INDEPENDENT);
        // Whenever this creature attacks while you have the most life or are tied for most life, you
        // draw a card and you lose 1 life.
        PermanentPredicate attacksWhileLeading = new PermanentAllOfPredicate(List.of(
                new PermanentIsSourcePermanentPredicate(),
                new PermanentAttacksWhileSourceControllerHasMostLifePredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(attacksWhileLeading, new DrawCardEffect()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(attacksWhileLeading, new LoseLifeEffect(1)));
    }
}
