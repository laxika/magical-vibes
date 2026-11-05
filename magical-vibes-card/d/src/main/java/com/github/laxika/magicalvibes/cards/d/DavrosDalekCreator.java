package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWhoLostLifeFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "1")
@CardRegistration(set = "WHO", collectorNumber = "192")
@CardRegistration(set = "WHO", collectorNumber = "407")
@CardRegistration(set = "WHO", collectorNumber = "540")
@CardRegistration(set = "WHO", collectorNumber = "606")
@CardRegistration(set = "WHO", collectorNumber = "998")
@CardRegistration(set = "WHO", collectorNumber = "1131")
public class DavrosDalekCreator extends Card {

    public DavrosDalekCreator() {
        // The "if" follows the effect, not the trigger condition, so it is not an intervening-if (CR 603.4):
        // the ability always triggers and the life-loss condition is checked on resolution.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new OpponentLostLifeThisTurn(3),
                SequenceEffect.of(
                        new CreateTokenEffect("Dalek", 3, 3, CardColor.BLACK,
                                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT)),
                        new EachOpponentWhoLostLifeFacesVillainousChoiceEffect(3))));
    }
}
