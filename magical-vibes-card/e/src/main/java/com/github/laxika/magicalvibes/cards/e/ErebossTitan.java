package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "94")
public class ErebossTitan extends Card {

    public ErebossTitan() {
        // As long as your opponents control no creatures, this creature has indestructible.
        addStaticEffect(new ConditionalEffect(
                        new NotCondition(new OpponentControlsPermanent(new PermanentIsCreaturePredicate())),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // Whenever a creature card leaves an opponent's graveyard, you may discard a card.
        // If you do, return this card from your graveyard to your hand. The ability functions
        // only while this card is in its owner's graveyard.
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE,
                ObjectRelation.OPPONENT_OWNS),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(
                        DiscardCardThenEffect.continuing(
                                null,
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "a card"),
                        "Discard a card to return Erebos's Titan from your graveyard to your hand?"));
    }
}
