package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "165")
public class SongMadTreachery extends Card {

    public SongMadTreachery() {
        setBackFaceCard(new SongMadRuins());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Song-Mad Treachery",
                        List.of(
                                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Song-Mad Ruins", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SongMadRuins";
    }
}
