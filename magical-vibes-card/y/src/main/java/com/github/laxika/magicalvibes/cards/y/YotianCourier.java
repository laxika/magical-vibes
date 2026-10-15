package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotChosenDuringLastCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "25")
public class YotianCourier extends Card {

    public YotianCourier() {
        PermanentCount powerstones = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.POWERSTONE), CountScope.CONTROLLER);
        CardAllOfPredicate nonlandCard = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        addEffect(EffectSlot.ON_ATTACK, new ChooseModeNotChosenDuringLastCombatEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a tapped Powerstone token.",
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a nonland card with mana value equal to the number of Powerstones you control.",
                        new SeekLibraryEffect(new Fixed(1), nonlandCard, LibrarySearchDestination.HAND,
                                new ManaValueBound(powerstones, true, 0))))));
    }
}
