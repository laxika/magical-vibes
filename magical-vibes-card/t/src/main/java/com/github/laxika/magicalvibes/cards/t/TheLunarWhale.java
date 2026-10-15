package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "60")
public class TheLunarWhale extends Card {

    public TheLunarWhale() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceAttackedThisTurn(), new PlayLandsFromTopOfLibraryEffect()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceAttackedThisTurn(), new AllowCastFromTopOfLibraryEffect(Set.of(
                        CardType.CREATURE,
                        CardType.ENCHANTMENT,
                        CardType.SORCERY,
                        CardType.INSTANT,
                        CardType.ARTIFACT,
                        CardType.PLANESWALKER,
                        CardType.BATTLE,
                        CardType.KINDRED))));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
