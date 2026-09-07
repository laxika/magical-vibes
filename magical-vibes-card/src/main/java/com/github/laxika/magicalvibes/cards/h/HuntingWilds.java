package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSearchedPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "130")
public class HuntingWilds extends Card {

    public HuntingWilds() {
        CardPredicate forestFilter = new CardSubtypePredicate(CardSubtype.FOREST);
        AnimatePermanentsEffect animation = new AnimatePermanentsEffect(
                3, 3, List.of(), Set.of(Keyword.HASTE), CardColor.GREEN, Set.of(),
                GrantScope.OWN_PERMANENTS, EffectDuration.PERMANENT);

        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{G}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new SearchLibraryEffect(new Fixed(2), forestFilter, LibrarySearchDestination.BATTLEFIELD_TAPPED),
                new SearchLibraryEffect(new Fixed(2), forestFilter,
                        LibrarySearchDestination.BATTLEFIELD_TAPPED, null, 1, false,
                        false, false, false, animation, LibrarySearchPlayer.CONTROLLER,
                        false, false, true)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(), new UntapSearchedPermanentsEffect()));
    }
}
