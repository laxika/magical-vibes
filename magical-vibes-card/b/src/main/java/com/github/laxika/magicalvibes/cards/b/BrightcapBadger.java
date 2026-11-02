package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FungusFrolic;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "28")
@CardRegistration(set = "BLC", collectorNumber = "62")
public class BrightcapBadger extends Card {

    public BrightcapBadger() {
        setBackFaceCard(new FungusFrolic());
        addCastingOption(new AdventureCast("{2}{G}"));

        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FUNGUS, CardSubtype.SAPROLING))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new CreateTokenEffect(
                "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "FungusFrolic";
    }
}
