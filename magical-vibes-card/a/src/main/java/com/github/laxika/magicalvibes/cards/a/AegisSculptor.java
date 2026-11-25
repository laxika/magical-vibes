package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "35")
public class AegisSculptor extends Card {

    public AegisSculptor() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(2));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(new GraveyardCardThreshold(2, new CardTruePredicate()))),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN),
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                        "Exile two cards from your graveyard?")));
    }
}
