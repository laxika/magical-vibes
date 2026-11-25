package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FlamethrowerSonata;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "159")
public class TorrentSculptor extends Card {

    public TorrentSculptor() {
        // Ward {2}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        FlamethrowerSonata backFace = new FlamethrowerSonata();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ExileUpToOneCardFromGraveyardEffect.oneMatching(instantOrSorcery()).from(Subjects.you()),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new HalvedRoundedUp(AmountThisWay.exiledManaValue()))));
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
