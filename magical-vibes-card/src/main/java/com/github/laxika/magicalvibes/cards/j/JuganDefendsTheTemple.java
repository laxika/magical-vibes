package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RemnantOfTheRisingStar;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "194")
public class JuganDefendsTheTemple extends Card {

    public JuganDefendsTheTemple() {
        setBackFaceCard(new RemnantOfTheRisingStar());

        CreateTokenEffect humanMonk = new CreateTokenEffect(
                CardType.CREATURE, 1, "Human Monk", 1, 1, CardColor.GREEN, null,
                List.of(CardSubtype.HUMAN, CardSubtype.MONK), Set.of(), Set.of(), false, false,
                Map.of(), List.of(new ActivatedAbility(
                        true, null,
                        List.of(new AwardManaEffect(ManaColor.GREEN)),
                        "{T}: Add {G}."
                )), false, false, false, 0, Set.of());

        addEffect(EffectSlot.SAGA_CHAPTER_I, humanMonk);
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "RemnantOfTheRisingStar";
    }
}
