package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "25")
@CardRegistration(set = "PIO", collectorNumber = "25")
@CardRegistration(set = "FDC", collectorNumber = "37")
public class LinvalaThePreserver extends Card {

    public LinvalaThePreserver() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new AnOpponentHasMoreLifeThanController()),
                new GainLifeEffect(5));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentControlsMoreCreatures(1)),
                new CreateTokenEffect(1, "Angel", 3, 3, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of()));
    }
}
