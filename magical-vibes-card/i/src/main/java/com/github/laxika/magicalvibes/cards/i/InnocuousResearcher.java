package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "38")
@CardRegistration(set = "MKC", collectorNumber = "348")
public class InnocuousResearcher extends Card {

    public InnocuousResearcher() {
        addTrigger(Triggers.attacks(),
                new ParleyEffect(CreateTokenEffect.ofClueToken(1)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(
                        SequenceEffect.of(
                                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                                        new PermanentIsLandPredicate())),
                                new PlayerRestrictionEffect(Subjects.you(),
                                        PlayerRestriction.castingSpells(), Duration.UNTIL_YOUR_NEXT_TURN)),
                        "Untap all lands you control?"));
    }
}
