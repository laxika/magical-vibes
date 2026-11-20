package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "165")
public class SongMadTreachery extends Card {

    public SongMadTreachery() {
        setBackFaceCard(new SongMadRuins());
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Song-Mad Treachery",
                        List.of(
                                new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                                new UntapPermanentsEffect(Subjects.target()),
                                new GrantKeywordEffect(Keyword.HASTE, Subjects.target())),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Song-Mad Ruins", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SongMadRuins";
    }
}
