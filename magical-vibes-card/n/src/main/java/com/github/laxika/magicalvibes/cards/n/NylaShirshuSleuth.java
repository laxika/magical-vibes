package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "109")
@CardRegistration(set = "TLE", collectorNumber = "190")
public class NylaShirshuSleuth extends Card {

    public NylaShirshuSleuth() {
        EventValue manaValue = new EventValue();
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect(
                        creature,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        SequenceEffect.of(
                                new LoseLifeEffect(manaValue, LoseLifeRecipient.CONTROLLER),
                                CreateTokenEffect.ofClueToken(manaValue))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new ControlsPermanentCountAtMost(0,
                                new PermanentHasSubtypePredicate(CardSubtype.CLUE)),
                        new PutTargetCardExiledWithSourceIntoOwnersHandEffect()));
    }
}
