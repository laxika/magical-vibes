package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FlamethrowerSonata;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "159")
public class TorrentSculptor extends Card {

    public TorrentSculptor() {
        // Ward {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        FlamethrowerSonata backFace = new FlamethrowerSonata();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect(instantOrSorcery()));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Torrent Sculptor", List.of()),
                new ChooseOneEffect.ChooseOneOption("Flamethrower Sonata",
                        backFace.getSpellEffects())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "FlamethrowerSonata";
    }

    private static CardAnyOfPredicate instantOrSorcery() {
        return new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
    }
}
