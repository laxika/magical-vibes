package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentFacesGenesisVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "69")
@CardRegistration(set = "WHO", collectorNumber = "674")
public class GenesisOfTheDaleks extends Card {

    private static final CreateTokenEffect DALEKS = new CreateTokenEffect(
            new CountersOnSource(CounterType.LORE), "Dalek", 3, 3, CardColor.BLACK,
            List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT));

    public GenesisOfTheDaleks() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, DALEKS);
        addEffect(EffectSlot.SAGA_CHAPTER_II, DALEKS);
        addEffect(EffectSlot.SAGA_CHAPTER_III, DALEKS);

        addEffect(EffectSlot.SAGA_CHAPTER_IV, new TargetOpponentFacesGenesisVillainousChoiceEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_IV, Set.of(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")));
    }
}
