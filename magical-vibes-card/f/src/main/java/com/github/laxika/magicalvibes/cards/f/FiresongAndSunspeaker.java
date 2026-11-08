package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "280")
@CardRegistration(set = "2X2", collectorNumber = "215")
@CardRegistration(set = "MUL", collectorNumber = "39")
@CardRegistration(set = "MUL", collectorNumber = "104")
@CardRegistration(set = "MUL", collectorNumber = "169")
public class FiresongAndSunspeaker extends Card {

    public FiresongAndSunspeaker() {
        addStaticEffect(new GrantLifelinkToControllerSpellsByColorEffect(CardColor.RED));
        // Whenever a white instant or sorcery spell causes you to gain life, this deals 3 damage to target
        // creature or player.
        addTrigger(Triggers.youGainLife().causedBySpell(new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))))),
                new DealDamageToTargetCreatureOrPlayerEffect(3));
    }
}
