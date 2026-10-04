package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "AFC", collectorNumber = "249")
@CardRegistration(set = "C20", collectorNumber = "289")
@CardRegistration(set = "MIC", collectorNumber = "176")
@CardRegistration(set = "C19", collectorNumber = "260")
@CardRegistration(set = "C18", collectorNumber = "266")
@CardRegistration(set = "PIP", collectorNumber = "272")
@CardRegistration(set = "PIP", collectorNumber = "800")
@CardRegistration(set = "BFZ", collectorNumber = "240")
public class MortuaryMire extends Card {

    public MortuaryMire() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build(),
                "Put target creature card from your graveyard on top of your library?"));
    }
}
