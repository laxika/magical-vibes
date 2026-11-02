package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "8")
public class GreedsGambit extends Card {

    public GreedsGambit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(3),
                new GainLifeEffect(6),
                new CreateTokenEffect(3, "Bat", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of())));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new LoseLifeEffect(2),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.CONTROLLER)));

        var creatures = new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), SequenceEffect.of(
                new DiscardEffect(3, DiscardRecipient.CONTROLLER),
                new LoseLifeEffect(6),
                new SacrificePermanentsEffect(3, creatures, SacrificeRecipient.CONTROLLER)));
    }
}
