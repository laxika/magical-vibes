package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "583")
public class GardensOfTranquilRepose extends Card {

    public GardensOfTranquilRepose() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, SequenceEffect.of(
                new ExileTriggeringCreatureAndTrackWithSourceEffect(),
                new ScryEffect(1, LibraryOwner.DYING_CREATURE_CONTROLLER)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenEffect(
                new Sum(new Fixed(1), new CardsExiledWithSource()),
                "Dalek", 3, 3, CardColor.BLACK,
                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT)));
    }
}
