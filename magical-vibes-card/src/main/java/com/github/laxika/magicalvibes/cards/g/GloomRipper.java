package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "102")
public class GloomRipper extends Card {

    public GloomRipper() {
        Sum elfCount = new Sum(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER),
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(elfCount, new Fixed(0)));
        target(TargetFilters.creatureAnOpponentControls(), 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(new Fixed(0), new Scaled(elfCount, -1)));
    }
}
