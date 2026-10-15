package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "57")
@CardRegistration(set = "NCC", collectorNumber = "157")
public class CrashTheParty extends Card {

    public CrashTheParty() {
        PermanentCount tappedCreatures = new PermanentCount(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate())), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                tappedCreatures, "Rhino Warrior", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.RHINO, CardSubtype.WARRIOR), Set.of(), Set.of()).withTapped(true));
    }
}
