package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "193")
public class TitaniaVoiceOfGaea extends Card {

    private static final String ARGOTH_NAME = "Argoth, Sanctum of Nature";

    public TitaniaVoiceOfGaea() {
        setBackFaceCard(new TitaniaGaeaIncarnate());

        // Whenever one or more land cards are put into your graveyard from anywhere, you gain 2 life.
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(2));

        // At the beginning of your upkeep, if there are four or more land cards in your graveyard
        // and you both own and control Titania and Argoth, exile them, then meld them.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(
                        new GraveyardCardThreshold(4, new CardTypePredicate(CardType.LAND)),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentNamedPredicate(ARGOTH_NAME),
                                new PermanentIsLandPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))))),
                new MeldWithNamedCreatureEffect(ARGOTH_NAME, new PermanentIsLandPredicate())));
    }

    @Override
    public String getBackFaceClassName() {
        return "TitaniaGaeaIncarnate";
    }
}
