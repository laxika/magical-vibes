package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHC", collectorNumber = "11")
public class SerpentsSoulJar extends Card {

    public SerpentsSoulJar() {
        addTrigger(Triggers.nontokenCreatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ELF),
                        ObjectRelation.YOU_CONTROL),
                new ExileTriggeringCreatureAndTrackWithSourceEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
                                new CardTypePredicate(CardType.CREATURE), false)),
                "{T}, Pay 2 life: Until end of turn, you may cast a creature spell from among cards "
                        + "exiled with this artifact."));
    }
}
